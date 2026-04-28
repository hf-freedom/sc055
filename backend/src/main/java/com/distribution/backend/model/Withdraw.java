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
public class Withdraw {
    private Long id;
    private String withdrawNo;
    private Long userId;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal actualAmount;
    private String bankName;
    private String bankAccount;
    private String bankAccountName;
    private WithdrawStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime paidAt;
    private LocalDateTime failedAt;
    private String failReason;
    private String remark;
}
