package com.pacefriends.api.trail.presentation;

import com.pacefriends.api.trail.domain.TrainingPathItem;

import java.time.LocalDateTime;

public record TrailItemResponse(
        int position,
        String description,
        int xpReward,
        String status,
        LocalDateTime completedAt
) {
    public static TrailItemResponse from(TrainingPathItem item) {
        return new TrailItemResponse(
                item.getPosition(),
                item.getDescription(),
                item.getXpReward(),
                item.getStatus().name(),
                item.getCompletedAt()
        );
    }
}
