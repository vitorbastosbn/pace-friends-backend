package com.pacefriends.api.trail.domain;

public class LevelUpResult {

    private final int previousLevel;
    private final int newLevel;
    private final String newLevelName;

    public LevelUpResult(int previousLevel, int newLevel, String newLevelName) {
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
        this.newLevelName = newLevelName;
    }

    public int getPreviousLevel() { return previousLevel; }
    public int getNewLevel() { return newLevel; }
    public String getNewLevelName() { return newLevelName; }
}
