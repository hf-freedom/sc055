package com.distribution.backend.repository;

import com.distribution.backend.model.Order;
import com.distribution.backend.model.OrderStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class OrderRepository {
    private static final Map<Long, Order> orders = new HashMap<>();
    private static final AtomicLong idGenerator = new AtomicLong(1);

    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(idGenerator.getAndIncrement());
        }
        if (order.getOrderNo() == null) {
            order.setOrderNo(generateOrderNo());
        }
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(LocalDateTime.now());
        }
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }
        orders.put(order.getId(), order);
        return order;
    }

    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String seqPart = String.format("%04d", idGenerator.get() % 10000);
        return "ORD" + datePart + seqPart;
    }

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(orders.get(id));
    }

    public Optional<Order> findByOrderNo(String orderNo) {
        return orders.values().stream()
                .filter(order -> order.getOrderNo().equals(orderNo))
                .findFirst();
    }

    public List<Order> findAll() {
        return new ArrayList<>(orders.values());
    }

    public List<Order> findByUserId(Long userId) {
        List<Order> result = new ArrayList<>();
        for (Order order : orders.values()) {
            if (userId.equals(order.getUserId())) {
                result.add(order);
            }
        }
        result.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
        return result;
    }

    public List<Order> findByStatus(OrderStatus status) {
        List<Order> result = new ArrayList<>();
        for (Order order : orders.values()) {
            if (order.getStatus() == status) {
                result.add(order);
            }
        }
        return result;
    }
}
