package com.pacefriends.api.challenge.event;

import java.util.UUID;

public record IndividualChallengeCreatedEvent(UUID userId, UUID challengeId) {
}
