package com.pacefriends.api.friendchallenge.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pacefriends.api.friendchallenge.application.CheckInWithUserName;

import java.time.LocalDate;
import java.util.UUID;

public record ChallengeDetailCheckInResponse(
        UUID id,
        @JsonProperty("participant_name") String participantName,
        @JsonProperty("distance_km") double distanceKm,
        LocalDate date,
        String status
) {
    public static ChallengeDetailCheckInResponse from(CheckInWithUserName view) {
        return new ChallengeDetailCheckInResponse(
                view.checkIn().id(),
                view.userName(),
                view.checkIn().distanceKm(),
                view.checkIn().checkInDate(),
                view.checkIn().status()
        );
    }
}
