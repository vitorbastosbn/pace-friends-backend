package com.pacefriends.api.challenge.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

interface ActivityJpaRepository extends JpaRepository<ActivityEntity, UUID> {

    List<ActivityEntity> findByChallengeIdOrderByActivityDateDesc(UUID challengeId);

    @Query("SELECT COALESCE(SUM(a.distanceKm), 0) FROM ActivityEntity a WHERE a.challengeId = :challengeId")
    BigDecimal sumDistanceByChallengeId(@Param("challengeId") UUID challengeId);

    @Query("SELECT DISTINCT a.activityDate FROM ActivityEntity a WHERE a.userId = :userId AND a.activityDate >= :weekStart AND a.activityDate <= :weekEnd")
    Set<LocalDate> findDistinctActivityDatesByUserAndWeek(@Param("userId") UUID userId, @Param("weekStart") LocalDate weekStart, @Param("weekEnd") LocalDate weekEnd);

    @Query("SELECT COUNT(DISTINCT a.activityDate) FROM ActivityEntity a WHERE a.userId = :userId AND a.activityDate >= :weekStart AND a.activityDate <= :weekEnd")
    int countDistinctActivityDatesByUserAndWeek(@Param("userId") UUID userId, @Param("weekStart") LocalDate weekStart, @Param("weekEnd") LocalDate weekEnd);

    long countByUserId(@Param("userId") UUID userId);
}
