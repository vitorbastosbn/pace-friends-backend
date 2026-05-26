package com.pacefriends.api.home.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pacefriends.api.home.domain.HomeSummary;

public record HomeSummaryResponse(
        StreakResponse streak,
        XpResponse xp,
        LevelResponse level,
        @JsonProperty("weekly_frequency") WeeklyFrequencyResponse weeklyFrequency,
        @JsonProperty("training_path") TrainingPathResponse trainingPath
) {
    public static HomeSummaryResponse from(HomeSummary summary) {
        return new HomeSummaryResponse(
                new StreakResponse(summary.streak().current(), summary.streak().unit()),
                new XpResponse(summary.xp().total()),
                new LevelResponse(summary.level().current(), summary.level().xpForNextLevel()),
                new WeeklyFrequencyResponse(
                        summary.weeklyFrequency().daysTrained(),
                        summary.weeklyFrequency().goal()),
                new TrainingPathResponse(
                        summary.trainingPath().currentLevel(),
                        summary.trainingPath().progressPercent(),
                        summary.trainingPath().available())
        );
    }

    public record StreakResponse(int current, String unit) {
    }

    public record XpResponse(int total) {
    }

    public record LevelResponse(
            int current,
            @JsonProperty("xp_for_next_level") Integer xpForNextLevel
    ) {
    }

    public record WeeklyFrequencyResponse(
            @JsonProperty("days_trained") int daysTrained,
            int goal
    ) {
    }

    public record TrainingPathResponse(
            @JsonProperty("current_level") String currentLevel,
            @JsonProperty("progress_percent") Integer progressPercent,
            boolean available
    ) {
    }
}
