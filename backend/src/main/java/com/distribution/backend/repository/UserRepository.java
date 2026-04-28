package com.distribution.backend.repository;

import com.distribution.backend.model.User;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepository {
    private static final Map<Long, User> users = new HashMap<>();
    private static final AtomicLong idGenerator = new AtomicLong(5);
    private static volatile boolean initialized = false;

    public UserRepository() {
        synchronized (UserRepository.class) {
            if (!initialized) {
                initData();
                initialized = true;
            }
        }
    }

    private void initData() {
        if (!users.isEmpty()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        saveInternal(User.builder()
                .id(1L)
                .username("admin")
                .password("123456")
                .realName("管理员")
                .phone("13800138000")
                .parentId(null)
                .totalCommission(BigDecimal.ZERO)
                .availableCommission(BigDecimal.ZERO)
                .pendingCommission(BigDecimal.ZERO)
                .createdAt(now)
                .updatedAt(now)
                .build());

        saveInternal(User.builder()
                .id(2L)
                .username("user1")
                .password("123456")
                .realName("用户一")
                .phone("13800138001")
                .parentId(null)
                .totalCommission(BigDecimal.ZERO)
                .availableCommission(BigDecimal.ZERO)
                .pendingCommission(BigDecimal.ZERO)
                .createdAt(now)
                .updatedAt(now)
                .build());

        saveInternal(User.builder()
                .id(3L)
                .username("user2")
                .password("123456")
                .realName("用户二")
                .phone("13800138002")
                .parentId(2L)
                .totalCommission(BigDecimal.ZERO)
                .availableCommission(BigDecimal.ZERO)
                .pendingCommission(BigDecimal.ZERO)
                .createdAt(now)
                .updatedAt(now)
                .build());

        saveInternal(User.builder()
                .id(4L)
                .username("user3")
                .password("123456")
                .realName("用户三")
                .phone("13800138003")
                .parentId(3L)
                .totalCommission(BigDecimal.ZERO)
                .availableCommission(BigDecimal.ZERO)
                .pendingCommission(BigDecimal.ZERO)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    private User saveInternal(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        user.setUpdatedAt(LocalDateTime.now());
        users.put(user.getId(), user);
        return user;
    }

    public User save(User user) {
        return saveInternal(user);
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public Optional<User> findByUsername(String username) {
        return users.values().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public List<User> findByParentId(Long parentId) {
        List<User> result = new ArrayList<>();
        for (User user : users.values()) {
            if (parentId.equals(user.getParentId())) {
                result.add(user);
            }
        }
        return result;
    }
}
