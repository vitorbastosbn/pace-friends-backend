package com.pacefriends.api.profile.presentation;

import java.util.UUID;

public record PublicProfileResponse(
        UUID id,
        String name,
        String avatarUrl,
        int totalVictories,
        int achievementsUnlocked
) {
}
