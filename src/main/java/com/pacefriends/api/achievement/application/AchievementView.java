package com.pacefriends.api.achievement.application;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AchievementView(
        UUID id,
        String slug,
        String name,
        String description,
        String iconKey,
        boolean unlocked,
        OffsetDateTime unlockedAt,
        Integer progressCurrent,
        Integer progressTotal
) {
}
