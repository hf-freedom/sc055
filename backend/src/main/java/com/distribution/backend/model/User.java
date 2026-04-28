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
public class User {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private Long parentId;
    private BigDecimal totalCommission;
    private BigDecimal availableCommission;
    private BigDecimal pendingCommission;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
