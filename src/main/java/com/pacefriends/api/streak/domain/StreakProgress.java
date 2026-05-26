package com.pacefriends.api.streak.domain;

public record StreakProgress(
        int targetFrequency,
        int daysCompletedThisWeek,
        int remainingDays
) {
}
