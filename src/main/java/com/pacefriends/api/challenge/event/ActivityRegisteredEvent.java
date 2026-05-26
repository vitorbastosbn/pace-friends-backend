package com.pacefriends.api.challenge.event;

import java.util.UUID;

public record ActivityRegisteredEvent(UUID userId, UUID challengeId) {
}
