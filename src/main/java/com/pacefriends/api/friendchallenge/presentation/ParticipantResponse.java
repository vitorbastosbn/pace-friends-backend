package com.pacefriends.api.friendchallenge.presentation;

import com.pacefriends.api.friendchallenge.domain.FriendChallengeParticipant;
import com.pacefriends.api.friendchallenge.domain.ParticipantRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ParticipantResponse(
        UUID userId,
        String name,
        ParticipantRole role,
        OffsetDateTime joinedAt
) {
    public static ParticipantResponse from(FriendChallengeParticipant participant) {
        return new ParticipantResponse(
                participant.userId(),
                participant.name(),
                participant.role(),
                participant.joinedAt()
        );
    }
}
