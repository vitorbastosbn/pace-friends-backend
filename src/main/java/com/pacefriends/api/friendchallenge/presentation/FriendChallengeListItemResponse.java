package com.pacefriends.api.friendchallenge.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pacefriends.api.friendchallenge.domain.FriendChallenge;
import com.pacefriends.api.friendchallenge.domain.ParticipantRole;

import java.time.LocalDate;
import java.util.UUID;

public record FriendChallengeListItemResponse(
        UUID id,
        String title,
        String status,
        @JsonProperty("start_date") LocalDate startDate,
        @JsonProperty("end_date") LocalDate endDate,
        @JsonProperty("participant_count") int participantCount,
        @JsonProperty("user_role") ParticipantRole userRole,
        @JsonProperty("user_rank_position") Integer userRankPosition
) {
    public static FriendChallengeListItemResponse from(FriendChallenge challenge, Integer userRankPosition) {
        return new FriendChallengeListItemResponse(
                challenge.id(),
                challenge.title(),
                challenge.status(),
                challenge.startDate(),
                challenge.endDate(),
                challenge.participantCount(),
                challenge.myRole(),
                userRankPosition
        );
    }
}
