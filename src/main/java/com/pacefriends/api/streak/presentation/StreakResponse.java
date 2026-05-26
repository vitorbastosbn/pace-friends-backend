package com.pacefriends.api.streak.presentation;

import com.pacefriends.api.streak.application.StreakView;

public record StreakResponse(
        int currentStreak,
        int targetFrequency,
        int daysCompletedThisWeek,
        int remainingDays,
        XpProgressResponse xpProgress,
        String lastResult
) {
    public static StreakResponse from(StreakView view) {
        return new StreakResponse(
                view.currentStreak(),
                view.targetFrequency(),
                view.daysCompletedThisWeek(),
                view.remainingDays(),
                XpProgressResponse.from(view.xpProgress()),
                view.lastResult() == null ? null : view.lastResult().name()
        );
    }
}
