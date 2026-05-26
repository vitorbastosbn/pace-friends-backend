package com.pacefriends.api.trail.infrastructure;

public class TrailUserStats {

    private final long activityCount;
    private final double totalDistanceKm;
    private final int currentStreak;
    private final int totalXp;
    private final int currentLevel;

    public TrailUserStats(long activityCount, double totalDistanceKm,
                          int currentStreak, int totalXp, int currentLevel) {
        this.activityCount = activityCount;
        this.totalDistanceKm = totalDistanceKm;
        this.currentStreak = currentStreak;
        this.totalXp = totalXp;
        this.currentLevel = currentLevel;
    }

    public long getActivityCount() { return activityCount; }
    public double getTotalDistanceKm() { return totalDistanceKm; }
    public int getCurrentStreak() { return currentStreak; }
    public int getTotalXp() { return totalXp; }
    public int getCurrentLevel() { return currentLevel; }
}
