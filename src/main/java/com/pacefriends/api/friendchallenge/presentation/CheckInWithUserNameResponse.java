package com.pacefriends.api.friendchallenge.presentation;

import com.pacefriends.api.friendchallenge.application.CheckInWithUserName;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CheckInWithUserNameResponse(
        UUID id,
        UUID challengeId,
        UUID userId,
        String userName,
        double distanceKm,
        int durationSeconds,
        long paceSecondsPerKm,
        LocalDate checkInDate,
        String notes,
        String status,
        OffsetDateTime createdAt
) {
    public static CheckInWithUserNameResponse from(CheckInWithUserName view) {
        return new CheckInWithUserNameResponse(
                view.checkIn().id(),
                view.checkIn().challengeId(),
                view.checkIn().userId(),
                view.userName(),
                view.checkIn().distanceKm(),
                view.checkIn().durationSeconds(),
                view.checkIn().paceSecondsPerKm(),
                view.checkIn().checkInDate(),
                view.checkIn().notes(),
                view.checkIn().status(),
                view.checkIn().createdAt()
        );
    }
}
