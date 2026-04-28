package com.distribution.backend.controller;

import com.distribution.backend.model.Commission;
import com.distribution.backend.service.CommissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/commissions")
@CrossOrigin(origins = "http://localhost:3004")
public class CommissionController {

    @Autowired
    private CommissionService commissionService;

    @GetMapping
    public ResponseEntity<List<Commission>> getAllCommissions() {
        List<Commission> commissions = commissionService.getAllCommissions();
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Commission> getCommissionById(@PathVariable Long id) {
        Optional<Commission> commissionOpt = commissionService.getCommissionById(id);
        return commissionOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Commission>> getCommissionsByUserId(@PathVariable Long userId) {
        List<Commission> commissions = commissionService.getCommissionsByUserId(userId);
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Commission>> getCommissionsByOrderId(@PathVariable Long orderId) {
        List<Commission> commissions = commissionService.getCommissionsByOrderId(orderId);
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/status/pending")
    public ResponseEntity<List<Commission>> getPendingCommissions() {
        List<Commission> commissions = commissionService.getPendingCommissions();
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/status/available")
    public ResponseEntity<List<Commission>> getAvailableCommissions() {
        List<Commission> commissions = commissionService.getAvailableCommissions();
        return ResponseEntity.ok(commissions);
    }

    @PostMapping("/settle-all")
    public ResponseEntity<Map<String, Object>> settleAllPending() {
        commissionService.settleAllPendingImmediately();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Settlement process executed");
        return ResponseEntity.ok(result);
    }
}
