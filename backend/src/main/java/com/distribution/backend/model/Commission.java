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
public class Commission {
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long fromUserId;
    private Integer level;
    private BigDecimal amount;
    private CommissionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime settledAt;
    private String remark;
    private Long relatedCommissionId;
}
