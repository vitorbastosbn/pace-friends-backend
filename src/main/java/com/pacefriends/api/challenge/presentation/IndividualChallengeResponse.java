package com.pacefriends.api.challenge.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pacefriends.api.challenge.domain.ChallengeProgress;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record IndividualChallengeResponse(
        UUID id,
        String title,
        @JsonProperty("goal_distance_km") BigDecimal goalDistanceKm,
        LocalDate deadline,
        String status,
        @JsonProperty("progress_km") BigDecimal progressKm,
        @JsonProperty("progress_percentage") BigDecimal progressPercentage
) {
    public static IndividualChallengeResponse from(ChallengeProgress progress) {
        return new IndividualChallengeResponse(
                progress.getChallenge().getId(),
                progress.getChallenge().getTitle(),
                progress.getChallenge().getGoalDistanceKm(),
                progress.getChallenge().getDeadline(),
                progress.getChallenge().getStatus().name(),
                progress.getProgressKm(),
                progress.getProgressPct()
        );
    }
}
