package com.pacefriends.api.challenge.presentation;

import com.pacefriends.api.challenge.domain.ChallengeProgress;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ChallengeProgressResponse(
        UUID id,
        String title,
        BigDecimal goalDistanceKm,
        LocalDate deadline,
        String status,
        BigDecimal progressKm,
        BigDecimal progressPct,
        LocalDateTime createdAt,
        List<ActivityResponse> activities
) {

    public static ChallengeProgressResponse from(ChallengeProgress progress) {
        return new ChallengeProgressResponse(
                progress.getChallenge().getId(),
                progress.getChallenge().getTitle(),
                progress.getChallenge().getGoalDistanceKm(),
                progress.getChallenge().getDeadline(),
                progress.getChallenge().getStatus().name(),
                progress.getProgressKm(),
                progress.getProgressPct(),
                progress.getChallenge().getCreatedAt(),
                null
        );
    }

    public static ChallengeProgressResponse from(ChallengeProgress progress, List<ActivityResponse> activities) {
        return new ChallengeProgressResponse(
                progress.getChallenge().getId(),
                progress.getChallenge().getTitle(),
                progress.getChallenge().getGoalDistanceKm(),
                progress.getChallenge().getDeadline(),
                progress.getChallenge().getStatus().name(),
                progress.getProgressKm(),
                progress.getProgressPct(),
                progress.getChallenge().getCreatedAt(),
                activities
        );
    }
}
