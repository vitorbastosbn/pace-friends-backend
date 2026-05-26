package com.pacefriends.api.trail.domain;

import java.time.LocalDateTime;

public class TrainingPathItem {

    private final int position;
    private final String description;
    private final int xpReward;
    private final ItemStatus status;
    private final LocalDateTime completedAt;

    public TrainingPathItem(int position, String description, int xpReward,
                             ItemStatus status, LocalDateTime completedAt) {
        this.position = position;
        this.description = description;
        this.xpReward = xpReward;
        this.status = status;
        this.completedAt = completedAt;
    }

    public int getPosition() { return position; }
    public String getDescription() { return description; }
    public int getXpReward() { return xpReward; }
    public ItemStatus getStatus() { return status; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
