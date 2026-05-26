package com.pacefriends.api.friendchallenge.domain;

import java.util.UUID;

public record RankingEntry(
        int position,
        UUID userId,
        String name,
        double score,
        int checkInCount
) {
}
