package com.distribution.backend.service;

import com.distribution.backend.model.Commission;
import com.distribution.backend.model.CommissionStatus;
import com.distribution.backend.model.User;
import com.distribution.backend.model.Withdraw;
import com.distribution.backend.model.WithdrawStatus;
import com.distribution.backend.repository.CommissionRepository;
import com.distribution.backend.repository.UserRepository;
import com.distribution.backend.repository.WithdrawRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WithdrawService {

    @Autowired
    private WithdrawRepository withdrawRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommissionRepository commissionRepository;

    private static final BigDecimal MIN_WITHDRAW_AMOUNT = new BigDecimal("10.00");

    public List<Withdraw> getAllWithdraws() {
        return withdrawRepository.findAll();
    }

    public Optional<Withdraw> getWithdrawById(Long id) {
        return withdrawRepository.findById(id);
    }

    public Optional<Withdraw> getWithdrawByWithdrawNo(String withdrawNo) {
        return withdrawRepository.findByWithdrawNo(withdrawNo);
    }

    public List<Withdraw> getWithdrawsByUserId(Long userId) {
        return withdrawRepository.findByUserId(userId);
    }

    public List<Withdraw> getPendingWithdraws() {
        return withdrawRepository.findByStatus(WithdrawStatus.PENDING);
    }

    public List<Withdraw> getFailedWithdraws() {
        return withdrawRepository.findByStatus(WithdrawStatus.FAILED);
    }

    @Transactional
    public Optional<Withdraw> applyWithdraw(Long userId, BigDecimal amount,
                                             String bankName, String bankAccount,
                                             String bankAccountName) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return Optional.empty();
        }

        User user = userOpt.get();

        if (amount.compareTo(MIN_WITHDRAW_AMOUNT) < 0) {
            return Optional.empty();
        }

        if (user.getAvailableCommission().compareTo(amount) < 0) {
            return Optional.empty();
        }

        Withdraw withdraw = Withdraw.builder()
                .userId(userId)
                .amount(amount)
                .fee(BigDecimal.ZERO)
                .actualAmount(amount)
                .bankName(bankName)
                .bankAccount(bankAccount)
                .bankAccountName(bankAccountName)
                .status(WithdrawStatus.PENDING)
                .build();

        withdraw = withdrawRepository.save(withdraw);

        return Optional.of(withdraw);
    }

    @Transactional
    public Optional<Withdraw> approveWithdraw(Long withdrawId) {
        Optional<Withdraw> withdrawOpt = withdrawRepository.findById(withdrawId);
        if (!withdrawOpt.isPresent()) {
            return Optional.empty();
        }

        Withdraw withdraw = withdrawOpt.get();
        if (withdraw.getStatus() != WithdrawStatus.PENDING) {
            return Optional.empty();
        }

        Optional<User> userOpt = userRepository.findById(withdraw.getUserId());
        if (!userOpt.isPresent()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        if (user.getAvailableCommission().compareTo(withdraw.getAmount()) < 0) {
            return Optional.empty();
        }

        user.setAvailableCommission(user.getAvailableCommission().subtract(withdraw.getAmount()));
        userRepository.save(user);

        withdraw.setStatus(WithdrawStatus.APPROVED);
        withdraw.setApprovedAt(LocalDateTime.now());
        withdraw = withdrawRepository.save(withdraw);

        return Optional.of(withdraw);
    }

    @Transactional
    public Optional<Withdraw> rejectWithdraw(Long withdrawId, String reason) {
        Optional<Withdraw> withdrawOpt = withdrawRepository.findById(withdrawId);
        if (!withdrawOpt.isPresent()) {
            return Optional.empty();
        }

        Withdraw withdraw = withdrawOpt.get();
        if (withdraw.getStatus() != WithdrawStatus.PENDING) {
            return Optional.empty();
        }

        withdraw.setStatus(WithdrawStatus.REJECTED);
        withdraw.setRejectedAt(LocalDateTime.now());
        withdraw.setRemark(reason);
        withdraw = withdrawRepository.save(withdraw);

        return Optional.of(withdraw);
    }

    @Transactional
    public Optional<Withdraw> payWithdraw(Long withdrawId) {
        Optional<Withdraw> withdrawOpt = withdrawRepository.findById(withdrawId);
        if (!withdrawOpt.isPresent()) {
            return Optional.empty();
        }

        Withdraw withdraw = withdrawOpt.get();
        if (withdraw.getStatus() != WithdrawStatus.APPROVED) {
            return Optional.empty();
        }

        withdraw.setStatus(WithdrawStatus.PAID);
        withdraw.setPaidAt(LocalDateTime.now());
        withdraw = withdrawRepository.save(withdraw);

        return Optional.of(withdraw);
    }

    @Transactional
    public Optional<Withdraw> markWithdrawFailed(Long withdrawId, String failReason) {
        Optional<Withdraw> withdrawOpt = withdrawRepository.findById(withdrawId);
        if (!withdrawOpt.isPresent()) {
            return Optional.empty();
        }

        Withdraw withdraw = withdrawOpt.get();
        if (withdraw.getStatus() != WithdrawStatus.APPROVED && withdraw.getStatus() != WithdrawStatus.PAID) {
            return Optional.empty();
        }

        withdraw.setStatus(WithdrawStatus.FAILED);
        withdraw.setFailedAt(LocalDateTime.now());
        withdraw.setFailReason(failReason);
        withdraw = withdrawRepository.save(withdraw);

        return Optional.of(withdraw);
    }

    @Transactional
    public Optional<Withdraw> compensateFailedWithdraw(Long withdrawId) {
        Optional<Withdraw> withdrawOpt = withdrawRepository.findById(withdrawId);
        if (!withdrawOpt.isPresent()) {
            return Optional.empty();
        }

        Withdraw withdraw = withdrawOpt.get();
        if (withdraw.getStatus() != WithdrawStatus.FAILED) {
            return Optional.empty();
        }

        Optional<User> userOpt = userRepository.findById(withdraw.getUserId());
        if (!userOpt.isPresent()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        user.setAvailableCommission(user.getAvailableCommission().add(withdraw.getAmount()));
        userRepository.save(user);

        withdraw.setStatus(WithdrawStatus.COMPENSATED);
        withdraw.setRemark("提现失败已退回佣金");
        withdraw = withdrawRepository.save(withdraw);

        return Optional.of(withdraw);
    }

    @Transactional
    public void compensateAllFailedWithdraws() {
        List<Withdraw> failedWithdraws = withdrawRepository.findByStatus(WithdrawStatus.FAILED);
        for (Withdraw withdraw : failedWithdraws) {
            compensateFailedWithdraw(withdraw.getId());
        }
    }
}
