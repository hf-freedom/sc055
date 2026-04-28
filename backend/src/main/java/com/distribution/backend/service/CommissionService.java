package com.distribution.backend.service;

import com.distribution.backend.model.Commission;
import com.distribution.backend.model.CommissionStatus;
import com.distribution.backend.model.Order;
import com.distribution.backend.model.OrderStatus;
import com.distribution.backend.model.User;
import com.distribution.backend.repository.CommissionRepository;
import com.distribution.backend.repository.OrderRepository;
import com.distribution.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommissionService {

    @Autowired
    private CommissionRepository commissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    public List<Commission> getAllCommissions() {
        return commissionRepository.findAll();
    }

    public Optional<Commission> getCommissionById(Long id) {
        return commissionRepository.findById(id);
    }

    public List<Commission> getCommissionsByUserId(Long userId) {
        return commissionRepository.findByUserId(userId);
    }

    public List<Commission> getCommissionsByOrderId(Long orderId) {
        return commissionRepository.findByOrderId(orderId);
    }

    public List<Commission> getPendingCommissions() {
        return commissionRepository.findByStatus(CommissionStatus.PENDING);
    }

    public List<Commission> getAvailableCommissions() {
        return commissionRepository.findByStatus(CommissionStatus.AVAILABLE);
    }

    @Transactional
    public void addPendingCommission(Long userId, BigDecimal amount) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPendingCommission(user.getPendingCommission().add(amount));
            userRepository.save(user);
        }
    }

    @Transactional
    public void settlePendingCommissions() {
        List<Order> completedOrders = orderRepository.findByStatus(OrderStatus.COMPLETED);
        LocalDateTime now = LocalDateTime.now();

        for (Order order : completedOrders) {
            if (order.getAfterSalesDeadline() != null &&
                    order.getAfterSalesDeadline().isBefore(now)) {
                settleOrderCommissions(order);
            }
        }
    }

    @Transactional
    public void settleAllPendingImmediately() {
        List<Order> completedOrders = orderRepository.findByStatus(OrderStatus.COMPLETED);

        for (Order order : completedOrders) {
            settleOrderCommissions(order);
        }
    }

    @Transactional
    public void settleOrderCommissions(Order order) {
        List<Commission> commissions = commissionRepository.findByOrderId(order.getId());

        for (Commission commission : commissions) {
            if (commission.getStatus() == CommissionStatus.PENDING) {
                commission.setStatus(CommissionStatus.AVAILABLE);
                commission.setSettledAt(LocalDateTime.now());
                commissionRepository.save(commission);

                Optional<User> userOpt = userRepository.findById(commission.getUserId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setPendingCommission(user.getPendingCommission().subtract(commission.getAmount()));
                    user.setAvailableCommission(user.getAvailableCommission().add(commission.getAmount()));
                    user.setTotalCommission(user.getTotalCommission().add(commission.getAmount()));
                    userRepository.save(user);
                }
            }
        }
    }

    @Transactional
    public Optional<Commission> markAsWithdrawing(Long commissionId) {
        Optional<Commission> commissionOpt = commissionRepository.findById(commissionId);
        if (!commissionOpt.isPresent()) {
            return Optional.empty();
        }

        Commission commission = commissionOpt.get();
        if (commission.getStatus() != CommissionStatus.AVAILABLE) {
            return Optional.empty();
        }

        commission.setStatus(CommissionStatus.WITHDRAWING);
        commission = commissionRepository.save(commission);
        return Optional.of(commission);
    }

    @Transactional
    public Optional<Commission> markAsWithdrawn(Long commissionId) {
        Optional<Commission> commissionOpt = commissionRepository.findById(commissionId);
        if (!commissionOpt.isPresent()) {
            return Optional.empty();
        }

        Commission commission = commissionOpt.get();
        if (commission.getStatus() != CommissionStatus.WITHDRAWING) {
            return Optional.empty();
        }

        commission.setStatus(CommissionStatus.WITHDRAWN);
        commission = commissionRepository.save(commission);
        return Optional.of(commission);
    }

    @Transactional
    public Optional<Commission> revertCommission(Long commissionId, String reason) {
        Optional<Commission> commissionOpt = commissionRepository.findById(commissionId);
        if (!commissionOpt.isPresent()) {
            return Optional.empty();
        }

        Commission commission = commissionOpt.get();
        CommissionStatus originalStatus = commission.getStatus();

        commission.setStatus(CommissionStatus.REVERTED);
        commission.setRemark(reason);
        commission = commissionRepository.save(commission);

        Optional<User> userOpt = userRepository.findById(commission.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (originalStatus == CommissionStatus.PENDING) {
                user.setPendingCommission(user.getPendingCommission().subtract(commission.getAmount()));
            } else if (originalStatus == CommissionStatus.AVAILABLE || originalStatus == CommissionStatus.WITHDRAWING) {
                user.setAvailableCommission(user.getAvailableCommission().subtract(commission.getAmount()));
                user.setTotalCommission(user.getTotalCommission().subtract(commission.getAmount()));
            }

            userRepository.save(user);
        }

        return Optional.of(commission);
    }

    @Transactional
    public Commission createNegativeCommission(Long orderId, String orderNo, Long userId,
                                                Long fromUserId, Integer level, BigDecimal amount,
                                                String remark, Long relatedCommissionId) {
        Commission negativeCommission = Commission.builder()
                .orderId(orderId)
                .orderNo(orderNo)
                .userId(userId)
                .fromUserId(fromUserId)
                .level(level)
                .amount(amount.negate())
                .status(CommissionStatus.REVERTED)
                .createdAt(LocalDateTime.now())
                .settledAt(LocalDateTime.now())
                .remark(remark)
                .relatedCommissionId(relatedCommissionId)
                .build();

        negativeCommission = commissionRepository.save(negativeCommission);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setTotalCommission(user.getTotalCommission().add(negativeCommission.getAmount()));
            userRepository.save(user);
        }

        return negativeCommission;
    }
}
