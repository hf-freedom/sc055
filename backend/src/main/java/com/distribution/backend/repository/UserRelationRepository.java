package com.distribution.backend.repository;

import com.distribution.backend.model.UserRelation;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRelationRepository {
    private static final Map<Long, UserRelation> relations = new HashMap<>();
    private static final AtomicLong idGenerator = new AtomicLong(1);

    public UserRelation save(UserRelation relation) {
        if (relation.getId() == null) {
            relation.setId(idGenerator.getAndIncrement());
        }
        if (relation.getCreatedAt() == null) {
            relation.setCreatedAt(LocalDateTime.now());
        }
        if (relation.getEffectiveTime() == null) {
            relation.setEffectiveTime(LocalDateTime.now());
        }
        if (relation.getIsActive() == null) {
            relation.setIsActive(true);
        }
        relations.put(relation.getId(), relation);
        return relation;
    }

    public Optional<UserRelation> findById(Long id) {
        return Optional.ofNullable(relations.get(id));
    }

    public List<UserRelation> findAll() {
        return new ArrayList<>(relations.values());
    }

    public List<UserRelation> findByUserId(Long userId) {
        List<UserRelation> result = new ArrayList<>();
        for (UserRelation relation : relations.values()) {
            if (userId.equals(relation.getUserId())) {
                result.add(relation);
            }
        }
        result.sort(Comparator.comparing(UserRelation::getCreatedAt).reversed());
        return result;
    }

    public Optional<UserRelation> findActiveByUserId(Long userId) {
        return relations.values().stream()
                .filter(r -> userId.equals(r.getUserId()))
                .filter(r -> Boolean.TRUE.equals(r.getIsActive()))
                .max(Comparator.comparing(UserRelation::getCreatedAt));
    }

    public void expireOldRelations(Long userId) {
        for (UserRelation relation : relations.values()) {
            if (userId.equals(relation.getUserId()) && Boolean.TRUE.equals(relation.getIsActive())) {
                relation.setIsActive(false);
                relation.setExpireTime(LocalDateTime.now());
            }
        }
    }
}
