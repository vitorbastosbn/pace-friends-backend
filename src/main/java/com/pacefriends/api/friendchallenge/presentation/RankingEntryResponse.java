package com.pacefriends.api.friendchallenge.presentation;

import com.pacefriends.api.friendchallenge.domain.RankingEntry;

import java.util.UUID;

public record RankingEntryResponse(
        int position,
        UUID userId,
        String name,
        double score,
        int checkInCount
) {
    public static RankingEntryResponse from(RankingEntry entry) {
        return new RankingEntryResponse(
                entry.position(),
                entry.userId(),
                entry.name(),
                entry.score(),
                entry.checkInCount()
        );
    }
}
