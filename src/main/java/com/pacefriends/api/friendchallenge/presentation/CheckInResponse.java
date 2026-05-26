package com.pacefriends.api.friendchallenge.presentation;

import com.pacefriends.api.friendchallenge.domain.FriendChallengeCheckIn;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CheckInResponse(
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
    public static CheckInResponse from(FriendChallengeCheckIn checkIn) {
        return new CheckInResponse(
                checkIn.id(),
                checkIn.challengeId(),
                checkIn.userId(),
                checkIn.distanceKm(),
                checkIn.durationSeconds(),
                checkIn.paceSecondsPerKm(),
                checkIn.checkInDate(),
                checkIn.notes(),
                checkIn.status(),
                checkIn.createdAt()
        );
    }
}
