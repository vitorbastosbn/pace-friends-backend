package com.pacefriends.api.trail.presentation;

import com.pacefriends.api.trail.domain.LevelUpResult;

public record LevelUpResponse(
        int previousLevel,
        int newLevel,
        String newLevelName
) {
    public static LevelUpResponse from(LevelUpResult result) {
        return new LevelUpResponse(
                result.getPreviousLevel(),
                result.getNewLevel(),
                result.getNewLevelName()
        );
    }
}
