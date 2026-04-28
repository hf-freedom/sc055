package com.distribution.backend.service;

import com.distribution.backend.model.*;
import com.distribution.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RefundService {

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CommissionRepository commissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CommissionService commissionService;

    public List<Refund> getAllRefunds() {
        return refundRepository.findAll();
    }

    public Optional<Refund> getRefundById(Long id) {
        return refundRepository.findById(id);
    }

    public Optional<Refund> getRefundByRefundNo(String refundNo) {
        return refundRepository.findByRefundNo(refundNo);
    }

    public List<Refund> getRefundsByOrderId(Long orderId) {
        return refundRepository.findByOrderId(orderId);
    }

    public List<Refund> getPendingRefunds() {
        return refundRepository.findByStatus(RefundStatus.PENDING);
    }

    @Transactional
    public Optional<Refund> applyRefund(Long orderId, BigDecimal refundAmount, String remark) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();
        if (order.getStatus() != OrderStatus.PAID &&
                order.getStatus() != OrderStatus.SHIPPED &&
                order.getStatus() != OrderStatus.COMPLETED) {
            return Optional.empty();
        }

        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0 ||
                refundAmount.compareTo(order.getTotalAmount()) > 0) {
            return Optional.empty();
        }

        Refund refund = Refund.builder()
                .orderId(orderId)
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .refundAmount(refundAmount)
                .status(RefundStatus.PENDING)
                .remark(remark)
                .build();

        refund = refundRepository.save(refund);

        order.setStatus(OrderStatus.REFUNDING);
        orderRepository.save(order);

        return Optional.of(refund);
    }

    @Transactional
    public Optional<Refund> approveRefund(Long refundId) {
        Optional<Refund> refundOpt = refundRepository.findById(refundId);
        if (!refundOpt.isPresent()) {
            return Optional.empty();
        }

        Refund refund = refundOpt.get();
        if (refund.getStatus() != RefundStatus.PENDING) {
            return Optional.empty();
        }

        refund.setStatus(RefundStatus.APPROVED);
        refund.setApprovedAt(LocalDateTime.now());
        refund = refundRepository.save(refund);

        return Optional.of(refund);
    }

    @Transactional
    public Optional<Refund> rejectRefund(Long refundId, String reason) {
        Optional<Refund> refundOpt = refundRepository.findById(refundId);
        if (!refundOpt.isPresent()) {
            return Optional.empty();
        }

        Refund refund = refundOpt.get();
        if (refund.getStatus() != RefundStatus.PENDING) {
            return Optional.empty();
        }

        refund.setStatus(RefundStatus.REJECTED);
        refund.setRemark(reason);
        refund = refundRepository.save(refund);

        Optional<Order> orderOpt = orderRepository.findById(refund.getOrderId());
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if (order.getCompletedAt() != null) {
                order.setStatus(OrderStatus.COMPLETED);
            } else if (order.getPaidAt() != null) {
                order.setStatus(OrderStatus.PAID);
            }
            orderRepository.save(order);
        }

        return Optional.of(refund);
    }

    @Transactional
    public Optional<Refund> completeRefund(Long refundId) {
        Optional<Refund> refundOpt = refundRepository.findById(refundId);
        if (!refundOpt.isPresent()) {
            return Optional.empty();
        }

        Refund refund = refundOpt.get();
        if (refund.getStatus() != RefundStatus.APPROVED) {
            return Optional.empty();
        }

        Optional<Order> orderOpt = orderRepository.findById(refund.getOrderId());
        if (!orderOpt.isPresent()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();

        BigDecimal refundRatio = refund.getRefundAmount().divide(
                order.getTotalAmount(), 4, BigDecimal.ROUND_HALF_UP);

        revertCommissions(order, refundRatio);

        Optional<Product> productOpt = productRepository.findById(order.getProductId());
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStock(product.getStock() + order.getQuantity());
            productRepository.save(product);
        }

        refund.setStatus(RefundStatus.COMPLETED);
        refund.setCompletedAt(LocalDateTime.now());
        refund = refundRepository.save(refund);

        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);

        return Optional.of(refund);
    }

    private void revertCommissions(Order order, BigDecimal refundRatio) {
        List<Commission> commissions = commissionRepository.findByOrderId(order.getId());

        for (Commission commission : commissions) {
            if (commission.getStatus() == CommissionStatus.REVERTED ||
                    commission.getStatus() == CommissionStatus.CANCELLED) {
                continue;
            }

            BigDecimal revertAmount = commission.getAmount().multiply(refundRatio);

            if (commission.getStatus() == CommissionStatus.PENDING ||
                    commission.getStatus() == CommissionStatus.AVAILABLE ||
                    commission.getStatus() == CommissionStatus.WITHDRAWING) {
                commissionService.revertCommission(commission.getId(),
                        "订单退款冲回，退款比例: " + refundRatio.multiply(new BigDecimal("100")) + "%");

                if (revertAmount.compareTo(commission.getAmount()) < 0) {
                    BigDecimal remainingAmount = commission.getAmount().subtract(revertAmount);
                    Commission remainingCommission = Commission.builder()
                            .orderId(order.getId())
                            .orderNo(order.getOrderNo())
                            .userId(commission.getUserId())
                            .fromUserId(commission.getFromUserId())
                            .level(commission.getLevel())
                            .amount(remainingAmount)
                            .status(commission.getStatus())
                            .remark("部分退款后剩余佣金")
                            .relatedCommissionId(commission.getId())
                            .build();
                    commissionRepository.save(remainingCommission);
                }
            } else if (commission.getStatus() == CommissionStatus.WITHDRAWN) {
                commissionService.createNegativeCommission(
                        order.getId(),
                        order.getOrderNo(),
                        commission.getUserId(),
                        commission.getFromUserId(),
                        commission.getLevel(),
                        revertAmount,
                        "已提现佣金退款冲回，生成负佣金账单",
                        commission.getId()
                );
            }
        }
    }
}
