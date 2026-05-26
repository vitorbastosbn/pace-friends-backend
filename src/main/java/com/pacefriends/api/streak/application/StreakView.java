package com.pacefriends.api.streak.application;

public record StreakView(
        int currentStreak,
        int targetFrequency,
        int daysCompletedThisWeek,
        int remainingDays,
        int potentialXp
) {
}
