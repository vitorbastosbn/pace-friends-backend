package com.pacefriends.api.trail.presentation;

import com.pacefriends.api.trail.domain.TrainingPathData;

import java.util.UUID;

public record TrainingPathResponse(
        UUID userId,
        int currentLevel,
        String currentLevelName,
        TrailPathResponse path,
        boolean canLevelUp,
        NextLevelRequirementsResponse nextLevelRequirements
) {
    public static TrainingPathResponse from(TrainingPathData data) {
        return new TrainingPathResponse(
                data.getUserId(),
                data.getCurrentLevel(),
                data.getCurrentLevelName(),
                TrailPathResponse.from(data.getPath()),
                data.isCanLevelUp(),
                NextLevelRequirementsResponse.from(data.getNextLevelRequirements())
        );
    }
}
