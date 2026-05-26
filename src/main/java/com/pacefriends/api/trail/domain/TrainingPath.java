package com.pacefriends.api.trail.domain;

import java.time.LocalDateTime;
import java.util.List;

public class TrainingPath {

    private final int level;
    private final int completedItems;
    private final int totalItems;
    private final LocalDateTime completedAt;
    private final boolean bonusXpAwarded;
    private final List<TrainingPathItem> items;

    public TrainingPath(int level, int completedItems, int totalItems,
                        LocalDateTime completedAt, boolean bonusXpAwarded,
                        List<TrainingPathItem> items) {
        this.level = level;
        this.completedItems = completedItems;
        this.totalItems = totalItems;
        this.completedAt = completedAt;
        this.bonusXpAwarded = bonusXpAwarded;
        this.items = List.copyOf(items);
    }

    public int getLevel() { return level; }
    public int getCompletedItems() { return completedItems; }
    public int getTotalItems() { return totalItems; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public boolean isBonusXpAwarded() { return bonusXpAwarded; }
    public List<TrainingPathItem> getItems() { return items; }
}
