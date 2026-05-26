package com.pacefriends.api.achievement.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AchievementJpaRepository extends JpaRepository<AchievementEntity, UUID> {

    List<AchievementEntity> findAllByOrderByName();

    Optional<AchievementEntity> findBySlug(String slug);

    List<AchievementEntity> findByCriteriaType(String criteriaType);
}
