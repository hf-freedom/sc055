package com.distribution.backend.controller;

import com.distribution.backend.model.Refund;
import com.distribution.backend.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/refunds")
@CrossOrigin(origins = "http://localhost:3004")
public class RefundController {

    @Autowired
    private RefundService refundService;

    @GetMapping
    public ResponseEntity<List<Refund>> getAllRefunds() {
        List<Refund> refunds = refundService.getAllRefunds();
        return ResponseEntity.ok(refunds);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Refund> getRefundById(@PathVariable Long id) {
        Optional<Refund> refundOpt = refundService.getRefundById(id);
        return refundOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/no/{refundNo}")
    public ResponseEntity<Refund> getRefundByRefundNo(@PathVariable String refundNo) {
        Optional<Refund> refundOpt = refundService.getRefundByRefundNo(refundNo);
        return refundOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Refund>> getRefundsByOrderId(@PathVariable Long orderId) {
        List<Refund> refunds = refundService.getRefundsByOrderId(orderId);
        return ResponseEntity.ok(refunds);
    }

    @GetMapping("/status/pending")
    public ResponseEntity<List<Refund>> getPendingRefunds() {
        List<Refund> refunds = refundService.getPendingRefunds();
        return ResponseEntity.ok(refunds);
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyRefund(@RequestBody Map<String, Object> request) {
        Long orderId = ((Number) request.get("orderId")).longValue();
        BigDecimal refundAmount = new BigDecimal(request.get("refundAmount").toString());
        String remark = (String) request.getOrDefault("remark", "");

        Optional<Refund> result = refundService.applyRefund(orderId, refundAmount, remark);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid order or refund amount");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveRefund(@PathVariable Long id) {
        Optional<Refund> result = refundService.approveRefund(id);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Refund not found or cannot be approved");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectRefund(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String reason = request.getOrDefault("reason", "");
        Optional<Refund> result = refundService.rejectRefund(id, reason);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Refund not found or cannot be rejected");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeRefund(@PathVariable Long id) {
        Optional<Refund> result = refundService.completeRefund(id);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Refund not found or cannot be completed");
            return ResponseEntity.badRequest().body(error);
        }
    }
}
