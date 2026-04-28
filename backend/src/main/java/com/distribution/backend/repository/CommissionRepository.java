package com.distribution.backend.repository;

import com.distribution.backend.model.Commission;
import com.distribution.backend.model.CommissionStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class CommissionRepository {
    private static final Map<Long, Commission> commissions = new HashMap<>();
    private static final AtomicLong idGenerator = new AtomicLong(1);

    public Commission save(Commission commission) {
        if (commission.getId() == null) {
            commission.setId(idGenerator.getAndIncrement());
        }
        if (commission.getCreatedAt() == null) {
            commission.setCreatedAt(LocalDateTime.now());
        }
        if (commission.getStatus() == null) {
            commission.setStatus(CommissionStatus.PENDING);
        }
        commissions.put(commission.getId(), commission);
        return commission;
    }

    public Optional<Commission> findById(Long id) {
        return Optional.ofNullable(commissions.get(id));
    }

    public List<Commission> findAll() {
        return new ArrayList<>(commissions.values());
    }

    public List<Commission> findByUserId(Long userId) {
        List<Commission> result = new ArrayList<>();
        for (Commission commission : commissions.values()) {
            if (userId.equals(commission.getUserId())) {
                result.add(commission);
            }
        }
        result.sort((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()));
        return result;
    }

    public List<Commission> findByOrderId(Long orderId) {
        List<Commission> result = new ArrayList<>();
        for (Commission commission : commissions.values()) {
            if (orderId.equals(commission.getOrderId())) {
                result.add(commission);
            }
        }
        return result;
    }

    public List<Commission> findByStatus(CommissionStatus status) {
        List<Commission> result = new ArrayList<>();
        for (Commission commission : commissions.values()) {
            if (commission.getStatus() == status) {
                result.add(commission);
            }
        }
        return result;
    }
}
