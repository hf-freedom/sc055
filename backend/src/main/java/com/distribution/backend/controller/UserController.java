package com.distribution.backend.controller;

import com.distribution.backend.model.User;
import com.distribution.backend.model.UserRelation;
import com.distribution.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3004")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userService.getUserById(id);
        return userOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/{userId}/bind-parent")
    public ResponseEntity<?> bindParent(
            @PathVariable Long userId,
            @RequestBody Map<String, Long> request) {
        Long parentId = request.get("parentId");
        if (parentId == null) {
            return ResponseEntity.badRequest().body("parentId is required");
        }

        Optional<User> result = userService.bindParent(userId, parentId);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid user or parent, or circular reference detected");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{userId}/relation")
    public ResponseEntity<UserRelation> getActiveRelation(@PathVariable Long userId) {
        Optional<UserRelation> relationOpt = userService.getActiveUserRelation(userId);
        return relationOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}/children")
    public ResponseEntity<List<User>> getDirectChildren(@PathVariable Long userId) {
        List<User> children = userService.getDirectChildren(userId);
        return ResponseEntity.ok(children);
    }

    @GetMapping("/{userId}/grandchildren")
    public ResponseEntity<List<User>> getGrandChildren(@PathVariable Long userId) {
        List<User> grandChildren = userService.getGrandChildren(userId);
        return ResponseEntity.ok(grandChildren);
    }
}
