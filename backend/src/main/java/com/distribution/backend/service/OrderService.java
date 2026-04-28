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
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRelationRepository userRelationRepository;

    @Autowired
    private CommissionRepository commissionRepository;

    @Autowired
    private CommissionService commissionService;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> getOrderByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo);
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Transactional
    public Optional<Order> createOrder(Long userId, Long productId, Integer quantity) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return Optional.empty();
        }

        Optional<Product> productOpt = productRepository.findById(productId);
        if (!productOpt.isPresent()) {
            return Optional.empty();
        }

        Product product = productOpt.get();
        if (product.getStock() < quantity) {
            return Optional.empty();
        }

        BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(quantity));

        Long parentId = null;
        Long grandparentId = null;
        BigDecimal firstLevelCommissionRate = BigDecimal.ZERO;
        BigDecimal secondLevelCommissionRate = BigDecimal.ZERO;
        BigDecimal firstLevelCommissionAmount = BigDecimal.ZERO;
        BigDecimal secondLevelCommissionAmount = BigDecimal.ZERO;

        Optional<UserRelation> relationOpt = userRelationRepository.findActiveByUserId(userId);
        if (relationOpt.isPresent()) {
            UserRelation relation = relationOpt.get();
            parentId = relation.getParentId();
            grandparentId = relation.getGrandparentId();

            if (Boolean.TRUE.equals(product.getIsDistributionEnabled())) {
                firstLevelCommissionRate = product.getFirstLevelCommissionRate();
                secondLevelCommissionRate = product.getSecondLevelCommissionRate();

                if (parentId != null) {
                    firstLevelCommissionAmount = totalAmount.multiply(firstLevelCommissionRate);
                }
                if (grandparentId != null) {
                    secondLevelCommissionAmount = totalAmount.multiply(secondLevelCommissionRate);
                }
            }
        }

        Order order = Order.builder()
                .userId(userId)
                .productId(productId)
                .productName(product.getName())
                .productPrice(product.getPrice())
                .quantity(quantity)
                .totalAmount(totalAmount)
                .parentId(parentId)
                .grandparentId(grandparentId)
                .firstLevelCommissionRate(firstLevelCommissionRate)
                .secondLevelCommissionRate(secondLevelCommissionRate)
                .firstLevelCommissionAmount(firstLevelCommissionAmount)
                .secondLevelCommissionAmount(secondLevelCommissionAmount)
                .status(OrderStatus.PENDING)
                .build();

        order = orderRepository.save(order);
        return Optional.of(order);
    }

    @Transactional
    public Optional<Order> payOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();
        if (order.getStatus() != OrderStatus.PENDING) {
            return Optional.empty();
        }

        Optional<Product> productOpt = productRepository.findById(order.getProductId());
        if (!productOpt.isPresent()) {
            return Optional.empty();
        }

        Product product = productOpt.get();
        if (product.getStock() < order.getQuantity()) {
            return Optional.empty();
        }

        product.setStock(product.getStock() - order.getQuantity());
        productRepository.save(product);

        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        order = orderRepository.save(order);

        createPendingCommissions(order);

        return Optional.of(order);
    }

    private void createPendingCommissions(Order order) {
        if (order.getParentId() != null && order.getFirstLevelCommissionAmount().compareTo(BigDecimal.ZERO) > 0) {
            Commission commission = Commission.builder()
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .userId(order.getParentId())
                    .fromUserId(order.getUserId())
                    .level(1)
                    .amount(order.getFirstLevelCommissionAmount())
                    .status(CommissionStatus.PENDING)
                    .remark("一级推广佣金")
                    .build();
            commission = commissionRepository.save(commission);

            commissionService.addPendingCommission(order.getParentId(), order.getFirstLevelCommissionAmount());
        }

        if (order.getGrandparentId() != null && order.getSecondLevelCommissionAmount().compareTo(BigDecimal.ZERO) > 0) {
            Commission commission = Commission.builder()
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .userId(order.getGrandparentId())
                    .fromUserId(order.getUserId())
                    .level(2)
                    .amount(order.getSecondLevelCommissionAmount())
                    .status(CommissionStatus.PENDING)
                    .remark("二级推广佣金")
                    .build();
            commission = commissionRepository.save(commission);

            commissionService.addPendingCommission(order.getGrandparentId(), order.getSecondLevelCommissionAmount());
        }
    }

    @Transactional
    public Optional<Order> completeOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.SHIPPED) {
            return Optional.empty();
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        order.setAfterSalesDeadline(LocalDateTime.now().plusDays(7));
        order = orderRepository.save(order);

        return Optional.of(order);
    }

    @Transactional
    public Optional<Order> shipOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();
        if (order.getStatus() != OrderStatus.PAID) {
            return Optional.empty();
        }

        order.setStatus(OrderStatus.SHIPPED);
        order = orderRepository.save(order);

        return Optional.of(order);
    }

    @Transactional
    public Optional<Order> cancelOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();
        if (order.getStatus() != OrderStatus.PENDING) {
            return Optional.empty();
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        return Optional.of(order);
    }
}
