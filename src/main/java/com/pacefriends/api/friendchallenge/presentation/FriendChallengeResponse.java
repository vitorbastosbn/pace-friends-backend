package com.pacefriends.api.friendchallenge.presentation;

import com.pacefriends.api.friendchallenge.domain.ChallengeType;
import com.pacefriends.api.friendchallenge.domain.FriendChallenge;
import com.pacefriends.api.friendchallenge.domain.ParticipantRole;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FriendChallengeResponse(
        UUID id,
        String title,
        String description,
        ChallengeType challengeType,
        BigDecimal goalValue,
        LocalDate startDate,
        LocalDate endDate,
        String inviteCode,
        String status,
        int participantCount,
        int maxParticipants,
        ParticipantRole myRole,
        OffsetDateTime createdAt
) {
    public static FriendChallengeResponse from(FriendChallenge challenge) {
        return new FriendChallengeResponse(
                challenge.id(),
                challenge.title(),
                challenge.description(),
                challenge.challengeType(),
                challenge.goalValue(),
                challenge.startDate(),
                challenge.endDate(),
                challenge.inviteCode(),
                challenge.status(),
                challenge.participantCount(),
                challenge.maxParticipants(),
                challenge.myRole(),
                challenge.createdAt()
        );
    }
}
