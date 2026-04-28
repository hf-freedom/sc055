package com.distribution.backend.controller;

import com.distribution.backend.model.Withdraw;
import com.distribution.backend.service.WithdrawService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/withdraws")
@CrossOrigin(origins = "http://localhost:3004")
public class WithdrawController {

    @Autowired
    private WithdrawService withdrawService;

    @GetMapping
    public ResponseEntity<List<Withdraw>> getAllWithdraws() {
        List<Withdraw> withdraws = withdrawService.getAllWithdraws();
        return ResponseEntity.ok(withdraws);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Withdraw> getWithdrawById(@PathVariable Long id) {
        Optional<Withdraw> withdrawOpt = withdrawService.getWithdrawById(id);
        return withdrawOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/no/{withdrawNo}")
    public ResponseEntity<Withdraw> getWithdrawByWithdrawNo(@PathVariable String withdrawNo) {
        Optional<Withdraw> withdrawOpt = withdrawService.getWithdrawByWithdrawNo(withdrawNo);
        return withdrawOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Withdraw>> getWithdrawsByUserId(@PathVariable Long userId) {
        List<Withdraw> withdraws = withdrawService.getWithdrawsByUserId(userId);
        return ResponseEntity.ok(withdraws);
    }

    @GetMapping("/status/pending")
    public ResponseEntity<List<Withdraw>> getPendingWithdraws() {
        List<Withdraw> withdraws = withdrawService.getPendingWithdraws();
        return ResponseEntity.ok(withdraws);
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyWithdraw(@RequestBody Map<String, Object> request) {
        Long userId = ((Number) request.get("userId")).longValue();
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String bankName = (String) request.get("bankName");
        String bankAccount = (String) request.get("bankAccount");
        String bankAccountName = (String) request.get("bankAccountName");

        Optional<Withdraw> result = withdrawService.applyWithdraw(
                userId, amount, bankName, bankAccount, bankAccountName);

        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid user, insufficient balance, or amount below minimum");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveWithdraw(@PathVariable Long id) {
        Optional<Withdraw> result = withdrawService.approveWithdraw(id);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Withdraw not found or cannot be approved");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectWithdraw(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String reason = request.getOrDefault("reason", "");
        Optional<Withdraw> result = withdrawService.rejectWithdraw(id, reason);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Withdraw not found or cannot be rejected");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payWithdraw(@PathVariable Long id) {
        Optional<Withdraw> result = withdrawService.payWithdraw(id);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Withdraw not found or cannot be paid");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/fail")
    public ResponseEntity<?> markWithdrawFailed(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String reason = request.getOrDefault("reason", "提现失败");
        Optional<Withdraw> result = withdrawService.markWithdrawFailed(id, reason);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Withdraw not found or cannot be marked as failed");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/compensate")
    public ResponseEntity<?> compensateWithdraw(@PathVariable Long id) {
        Optional<Withdraw> result = withdrawService.compensateFailedWithdraw(id);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Withdraw not found or cannot be compensated");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/compensate-all")
    public ResponseEntity<Map<String, Object>> compensateAllFailed() {
        withdrawService.compensateAllFailedWithdraws();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Compensation process executed");
        return ResponseEntity.ok(result);
    }
}
