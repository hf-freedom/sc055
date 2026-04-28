package com.distribution.backend.repository;

import com.distribution.backend.model.Withdraw;
import com.distribution.backend.model.WithdrawStatus;
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
public class WithdrawRepository {
    private static final Map<Long, Withdraw> withdraws = new HashMap<>();
    private static final AtomicLong idGenerator = new AtomicLong(1);

    public Withdraw save(Withdraw withdraw) {
        if (withdraw.getId() == null) {
            withdraw.setId(idGenerator.getAndIncrement());
        }
        if (withdraw.getWithdrawNo() == null) {
            withdraw.setWithdrawNo(generateWithdrawNo());
        }
        if (withdraw.getCreatedAt() == null) {
            withdraw.setCreatedAt(LocalDateTime.now());
        }
        if (withdraw.getStatus() == null) {
            withdraw.setStatus(WithdrawStatus.PENDING);
        }
        withdraws.put(withdraw.getId(), withdraw);
        return withdraw;
    }

    private String generateWithdrawNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String seqPart = String.format("%04d", idGenerator.get() % 10000);
        return "WD" + datePart + seqPart;
    }

    public Optional<Withdraw> findById(Long id) {
        return Optional.ofNullable(withdraws.get(id));
    }

    public Optional<Withdraw> findByWithdrawNo(String withdrawNo) {
        return withdraws.values().stream()
                .filter(withdraw -> withdraw.getWithdrawNo().equals(withdrawNo))
                .findFirst();
    }

    public List<Withdraw> findAll() {
        return new ArrayList<>(withdraws.values());
    }

    public List<Withdraw> findByUserId(Long userId) {
        List<Withdraw> result = new ArrayList<>();
        for (Withdraw withdraw : withdraws.values()) {
            if (userId.equals(withdraw.getUserId())) {
                result.add(withdraw);
            }
        }
        result.sort((w1, w2) -> w2.getCreatedAt().compareTo(w1.getCreatedAt()));
        return result;
    }

    public List<Withdraw> findByStatus(WithdrawStatus status) {
        List<Withdraw> result = new ArrayList<>();
        for (Withdraw withdraw : withdraws.values()) {
            if (withdraw.getStatus() == status) {
                result.add(withdraw);
            }
        }
        return result;
    }
}
