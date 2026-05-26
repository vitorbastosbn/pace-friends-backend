package com.pacefriends.api.trail.presentation;

import com.pacefriends.api.trail.domain.TrainingPath;

import java.time.LocalDateTime;
import java.util.List;

public record TrailPathResponse(
        int level,
        int completedItems,
        int totalItems,
        LocalDateTime completedAt,
        boolean bonusXpAwarded,
        List<TrailItemResponse> items
) {
    public static TrailPathResponse from(TrainingPath path) {
        List<TrailItemResponse> itemResponses = path.getItems().stream()
                .map(TrailItemResponse::from)
                .toList();
        return new TrailPathResponse(
                path.getLevel(),
                path.getCompletedItems(),
                path.getTotalItems(),
                path.getCompletedAt(),
                path.isBonusXpAwarded(),
                itemResponses
        );
    }
}
