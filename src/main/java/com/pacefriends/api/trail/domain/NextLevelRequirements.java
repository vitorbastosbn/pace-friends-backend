package com.pacefriends.api.trail.domain;

public class NextLevelRequirements {

    private final boolean pathCompleted;
    private final int streakWeeksRequired;
    private final int streakWeeksCompleted;
    private final int xpRequired;
    private final int xpCurrent;

    public NextLevelRequirements(boolean pathCompleted, int streakWeeksRequired,
                                  int streakWeeksCompleted, int xpRequired, int xpCurrent) {
        this.pathCompleted = pathCompleted;
        this.streakWeeksRequired = streakWeeksRequired;
        this.streakWeeksCompleted = streakWeeksCompleted;
        this.xpRequired = xpRequired;
        this.xpCurrent = xpCurrent;
    }

    public boolean isPathCompleted() { return pathCompleted; }
    public int getStreakWeeksRequired() { return streakWeeksRequired; }
    public int getStreakWeeksCompleted() { return streakWeeksCompleted; }
    public int getXpRequired() { return xpRequired; }
    public int getXpCurrent() { return xpCurrent; }
}
