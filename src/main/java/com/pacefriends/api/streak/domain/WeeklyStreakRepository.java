package com.pacefriends.api.streak.domain;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface WeeklyStreakRepository {
    WeeklyStreak save(WeeklyStreak streak);
    Optional<WeeklyStreak> findByUserIdAndWeekStart(UUID userId, LocalDate weekStart);
}
