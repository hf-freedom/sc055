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
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Boolean isDistributionEnabled;
    private BigDecimal firstLevelCommissionRate;
    private BigDecimal secondLevelCommissionRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
