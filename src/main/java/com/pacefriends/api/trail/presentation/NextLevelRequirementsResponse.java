package com.pacefriends.api.trail.presentation;

import com.pacefriends.api.trail.domain.NextLevelRequirements;

public record NextLevelRequirementsResponse(
        boolean pathCompleted,
        int streakWeeksRequired,
        int streakWeeksCompleted,
        int xpRequired,
        int xpCurrent
) {
    public static NextLevelRequirementsResponse from(NextLevelRequirements req) {
        return new NextLevelRequirementsResponse(
                req.isPathCompleted(),
                req.getStreakWeeksRequired(),
                req.getStreakWeeksCompleted(),
                req.getXpRequired(),
                req.getXpCurrent()
        );
    }
}
