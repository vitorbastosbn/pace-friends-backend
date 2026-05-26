package com.pacefriends.api.profile.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

interface UserSettingsJpaRepository extends JpaRepository<UserSettingsEntity, UUID> {

    @Query("""
            SELECT s FROM UserSettingsEntity s
            WHERE s.userId = :userId
              AND s.effectiveFrom <= :today
            ORDER BY s.effectiveFrom DESC
            LIMIT 1
            """)
    Optional<UserSettingsEntity> findActiveByUserId(
            @Param("userId") UUID userId,
            @Param("today") LocalDate today);
}
