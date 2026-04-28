package com.distribution.backend.repository;

import com.distribution.backend.model.Refund;
import com.distribution.backend.model.RefundStatus;
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
public class RefundRepository {
    private static final Map<Long, Refund> refunds = new HashMap<>();
    private static final AtomicLong idGenerator = new AtomicLong(1);

    public Refund save(Refund refund) {
        if (refund.getId() == null) {
            refund.setId(idGenerator.getAndIncrement());
        }
        if (refund.getRefundNo() == null) {
            refund.setRefundNo(generateRefundNo());
        }
        if (refund.getCreatedAt() == null) {
            refund.setCreatedAt(LocalDateTime.now());
        }
        if (refund.getStatus() == null) {
            refund.setStatus(RefundStatus.PENDING);
        }
        refunds.put(refund.getId(), refund);
        return refund;
    }

    private String generateRefundNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String seqPart = String.format("%04d", idGenerator.get() % 10000);
        return "RF" + datePart + seqPart;
    }

    public Optional<Refund> findById(Long id) {
        return Optional.ofNullable(refunds.get(id));
    }

    public Optional<Refund> findByRefundNo(String refundNo) {
        return refunds.values().stream()
                .filter(refund -> refund.getRefundNo().equals(refundNo))
                .findFirst();
    }

    public List<Refund> findAll() {
        return new ArrayList<>(refunds.values());
    }

    public List<Refund> findByOrderId(Long orderId) {
        List<Refund> result = new ArrayList<>();
        for (Refund refund : refunds.values()) {
            if (orderId.equals(refund.getOrderId())) {
                result.add(refund);
            }
        }
        return result;
    }

    public List<Refund> findByStatus(RefundStatus status) {
        List<Refund> result = new ArrayList<>();
        for (Refund refund : refunds.values()) {
            if (refund.getStatus() == status) {
                result.add(refund);
            }
        }
        return result;
    }
}
