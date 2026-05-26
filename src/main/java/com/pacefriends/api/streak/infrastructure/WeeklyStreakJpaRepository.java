package com.pacefriends.api.streak.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface WeeklyStreakJpaRepository extends JpaRepository<WeeklyStreakEntity, UUID> {

    Optional<WeeklyStreakEntity> findByUserIdAndWeekStartDate(UUID userId, LocalDate weekStartDate);

    List<WeeklyStreakEntity> findByUserIdOrderByWeekStartDateDesc(UUID userId);

    Optional<WeeklyStreakEntity> findFirstByUserIdOrderByWeekStartDateDesc(UUID userId);
}
