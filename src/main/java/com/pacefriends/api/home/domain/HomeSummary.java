package com.pacefriends.api.home.domain;

public record HomeSummary(
        Streak streak,
        Xp xp,
        Level level,
        WeeklyFrequency weeklyFrequency,
        TrainingPath trainingPath
) {
    public record Streak(int current, String unit) {
    }

    public record Xp(int total) {
    }

    public record Level(int current, Integer xpForNextLevel) {
    }

    public record WeeklyFrequency(int daysTrained, int goal) {
    }

    public record TrainingPath(String currentLevel, Integer progressPercent, boolean available) {
        public static TrainingPath unavailable() {
            return new TrainingPath(null, null, false);
        }
    }
}
