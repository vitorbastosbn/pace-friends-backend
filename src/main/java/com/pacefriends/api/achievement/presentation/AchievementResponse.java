package com.pacefriends.api.achievement.presentation;

import com.pacefriends.api.achievement.application.AchievementView;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AchievementResponse(
        UUID id,
        String slug,
        String name,
        String description,
        String iconKey,
        boolean unlocked,
        OffsetDateTime unlockedAt,
        AchievementProgressResponse progress
) {
    static AchievementResponse from(AchievementView view) {
        AchievementProgressResponse progress = null;
        if (view.progressCurrent() != null && view.progressTotal() != null) {
            progress = new AchievementProgressResponse(view.progressCurrent(), view.progressTotal());
        }
        return new AchievementResponse(
                view.id(),
                view.slug(),
                view.name(),
                view.description(),
                view.iconKey(),
                view.unlocked(),
                view.unlockedAt(),
                progress
        );
    }
}
