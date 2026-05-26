package com.pacefriends.api.streak.application;

import com.pacefriends.api.streak.domain.XpCalculation;
import com.pacefriends.api.streak.domain.StreakResult;

public record StreakView(
        int currentStreak,
        int targetFrequency,
        int daysCompletedThisWeek,
        int remainingDays,
        XpCalculation xpProgress,
        StreakResult lastResult
) {
}
