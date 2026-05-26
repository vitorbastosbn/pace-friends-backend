package com.pacefriends.api.profile.domain;

public class ProfileStats {

    private final int totalXp;
    private final int currentStreak;
    private final int achievementsUnlocked;
    private final int totalVictories;

    public ProfileStats(int totalXp, int currentStreak, int achievementsUnlocked, int totalVictories) {
        this.totalXp = totalXp;
        this.currentStreak = currentStreak;
        this.achievementsUnlocked = achievementsUnlocked;
        this.totalVictories = totalVictories;
    }

    public static ProfileStats empty() {
        return new ProfileStats(0, 0, 0, 0);
    }

    public int getTotalXp() {
        return totalXp;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getAchievementsUnlocked() {
        return achievementsUnlocked;
    }

    public int getTotalVictories() {
        return totalVictories;
    }
}
