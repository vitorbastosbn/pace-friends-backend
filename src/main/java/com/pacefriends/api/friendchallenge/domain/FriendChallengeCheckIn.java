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
    public static final String STATUS_VALID = "VALID";
    public static final String STATUS_REMOVED_BY_CREATOR = "REMOVED_BY_CREATOR";
    public static final String STATUS_REMOVED_BY_LEAVE = "REMOVED_BY_LEAVE";
    public static final String STATUS_REMOVED_BY_DELETE = "REMOVED_BY_DELETE";
}
