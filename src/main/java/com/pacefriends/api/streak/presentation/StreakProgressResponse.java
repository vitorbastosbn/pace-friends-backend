package com.pacefriends.api.streak.presentation;

import com.pacefriends.api.streak.application.StreakView;

public record StreakProgressResponse(
        int targetFrequency,
        int daysCompletedThisWeek,
        int remainingDays
) {
    public static StreakProgressResponse from(StreakView view) {
        return new StreakProgressResponse(
                view.targetFrequency(),
                view.daysCompletedThisWeek(),
                view.remainingDays()
        );
    }
}
