package com.pacefriends.api.trail.domain;

import java.util.UUID;

public class TrainingPathData {

    private final UUID userId;
    private final int currentLevel;
    private final String currentLevelName;
    private final TrainingPath path;
    private final boolean canLevelUp;
    private final NextLevelRequirements nextLevelRequirements;

    public TrainingPathData(UUID userId, int currentLevel, String currentLevelName,
                             TrainingPath path, boolean canLevelUp,
                             NextLevelRequirements nextLevelRequirements) {
        this.userId = userId;
        this.currentLevel = currentLevel;
        this.currentLevelName = currentLevelName;
        this.path = path;
        this.canLevelUp = canLevelUp;
        this.nextLevelRequirements = nextLevelRequirements;
    }

    public UUID getUserId() { return userId; }
    public int getCurrentLevel() { return currentLevel; }
    public String getCurrentLevelName() { return currentLevelName; }
    public TrainingPath getPath() { return path; }
    public boolean isCanLevelUp() { return canLevelUp; }
    public NextLevelRequirements getNextLevelRequirements() { return nextLevelRequirements; }
}
