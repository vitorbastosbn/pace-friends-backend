package com.pacefriends.api.challenge.presentation;

import com.pacefriends.api.challenge.domain.Challenge;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChallengeResponse(
        UUID id,
        String title,
        BigDecimal goalDistanceKm,
        LocalDate deadline,
        String status,
        LocalDateTime createdAt
) {

    public static ChallengeResponse from(Challenge challenge) {
        return new ChallengeResponse(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getGoalDistanceKm(),
                challenge.getDeadline(),
                challenge.getStatus().name(),
                challenge.getCreatedAt()
        );
    }
}
