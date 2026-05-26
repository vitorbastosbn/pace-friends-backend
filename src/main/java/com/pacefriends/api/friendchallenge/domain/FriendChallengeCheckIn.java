package com.pacefriends.api.friendchallenge.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FriendChallengeCheckIn(
        UUID id,
        UUID challengeId,
        UUID userId,
        double distanceKm,
        int durationSeconds,
        long paceSecondsPerKm,
        LocalDate checkInDate,
        String notes,
        String status,
        OffsetDateTime createdAt
) {
}
