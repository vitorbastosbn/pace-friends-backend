package com.pacefriends.api.streak.presentation;

import com.pacefriends.api.streak.application.StreakView;

public record StreakResponse(
        int currentStreak,
        int targetFrequency,
        int daysCompletedThisWeek,
        int remainingDays,
        int potentialXp
) {
    public static StreakResponse from(StreakView view) {
        return new StreakResponse(
                view.currentStreak(),
                view.targetFrequency(),
                view.daysCompletedThisWeek(),
                view.remainingDays(),
                view.potentialXp()
        );
    }
}
