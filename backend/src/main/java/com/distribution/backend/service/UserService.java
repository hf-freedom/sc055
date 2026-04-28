package com.distribution.backend.service;

import com.distribution.backend.model.User;
import com.distribution.backend.model.UserRelation;
import com.distribution.backend.repository.UserRelationRepository;
import com.distribution.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRelationRepository userRelationRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User createUser(User user) {
        if (user.getTotalCommission() == null) {
            user.setTotalCommission(BigDecimal.ZERO);
        }
        if (user.getAvailableCommission() == null) {
            user.setAvailableCommission(BigDecimal.ZERO);
        }
        if (user.getPendingCommission() == null) {
            user.setPendingCommission(BigDecimal.ZERO);
        }
        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> bindParent(Long userId, Long parentId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return Optional.empty();
        }

        Optional<User> parentOpt = userRepository.findById(parentId);
        if (!parentOpt.isPresent()) {
            return Optional.empty();
        }

        User user = userOpt.get();

        if (user.getParentId() != null) {
            return Optional.of(user);
        }

        if (userId.equals(parentId)) {
            return Optional.empty();
        }

        if (isDescendant(userId, parentId)) {
            return Optional.empty();
        }

        user.setParentId(parentId);
        user = userRepository.save(user);

        createUserRelation(user);

        return Optional.of(user);
    }

    private boolean isDescendant(Long userId, Long parentId) {
        User ancestor = userRepository.findById(parentId).orElse(null);
        while (ancestor != null && ancestor.getParentId() != null) {
            if (ancestor.getParentId().equals(userId)) {
                return true;
            }
            ancestor = userRepository.findById(ancestor.getParentId()).orElse(null);
        }
        return false;
    }

    private void createUserRelation(User user) {
        userRelationRepository.expireOldRelations(user.getId());

        Long parentId = user.getParentId();
        Long grandparentId = null;

        if (parentId != null) {
            Optional<User> parentOpt = userRepository.findById(parentId);
            if (parentOpt.isPresent()) {
                grandparentId = parentOpt.get().getParentId();
            }
        }

        UserRelation relation = UserRelation.builder()
                .userId(user.getId())
                .parentId(parentId)
                .grandparentId(grandparentId)
                .isActive(true)
                .build();

        userRelationRepository.save(relation);
    }

    public Optional<UserRelation> getActiveUserRelation(Long userId) {
        return userRelationRepository.findActiveByUserId(userId);
    }

    public List<User> getDirectChildren(Long userId) {
        return userRepository.findByParentId(userId);
    }

    public List<User> getGrandChildren(Long userId) {
        List<User> children = userRepository.findByParentId(userId);
        return children.stream()
                .flatMap(child -> userRepository.findByParentId(child.getId()).stream())
                .collect(java.util.stream.Collectors.toList());
    }
}
