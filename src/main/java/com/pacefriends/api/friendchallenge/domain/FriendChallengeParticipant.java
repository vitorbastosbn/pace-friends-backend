package com.pacefriends.api.friendchallenge.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FriendChallengeParticipant(
        UUID userId,
        String name,
        ParticipantRole role,
        OffsetDateTime joinedAt
) {
}
