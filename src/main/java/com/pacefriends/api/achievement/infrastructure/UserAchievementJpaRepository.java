package com.pacefriends.api.achievement.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserAchievementJpaRepository extends JpaRepository<UserAchievementEntity, UUID> {

    List<UserAchievementEntity> findByUserId(UUID userId);

    int countByUserId(UUID userId);

    boolean existsByUserIdAndAchievementId(UUID userId, UUID achievementId);

    @Modifying
    @Query(value = """
            INSERT INTO user_achievements (user_id, achievement_id)
            VALUES (:userId, :achievementId)
            ON CONFLICT (user_id, achievement_id) DO NOTHING
            """, nativeQuery = true)
    void insertIfNotExists(@Param("userId") UUID userId, @Param("achievementId") UUID achievementId);
}
