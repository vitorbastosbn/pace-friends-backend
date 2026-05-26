package com.pacefriends.api.achievement.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserAchievement(
        UUID id,
        UUID userId,
        UUID achievementId,
        OffsetDateTime unlockedAt
) {
}
