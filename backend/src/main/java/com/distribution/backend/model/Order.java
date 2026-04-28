package com.distribution.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Long parentId;
    private Long grandparentId;
    private BigDecimal firstLevelCommissionRate;
    private BigDecimal secondLevelCommissionRate;
    private BigDecimal firstLevelCommissionAmount;
    private BigDecimal secondLevelCommissionAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime completedAt;
    private LocalDateTime afterSalesDeadline;
}
