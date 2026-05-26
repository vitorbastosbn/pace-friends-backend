package com.pacefriends.api.friendchallenge.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record FriendChallenge(
        UUID id,
        UUID creatorId,
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
        OffsetDateTime createdAt,
        List<FriendChallengeParticipant> participants
) {
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_AUDIT = "AUDIT";
    public static final String STATUS_FINISHED = "FINISHED";

    public FriendChallenge withStatus(String nextStatus) {
        return new FriendChallenge(
                id, creatorId, title, description, challengeType, goalValue,
                startDate, endDate, inviteCode, nextStatus, participantCount,
                maxParticipants, myRole, createdAt, participants
        );
    }
}
