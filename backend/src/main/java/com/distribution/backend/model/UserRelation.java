package com.distribution.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRelation {
    private Long id;
    private Long userId;
    private Long parentId;
    private Long grandparentId;
    private LocalDateTime createdAt;
    private LocalDateTime effectiveTime;
    private LocalDateTime expireTime;
    private Boolean isActive;
}
