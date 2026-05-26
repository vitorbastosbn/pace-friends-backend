package com.pacefriends.api.friendchallenge.presentation;

import com.pacefriends.api.friendchallenge.application.RankingView;
import com.pacefriends.api.friendchallenge.domain.ChallengeType;

import java.util.List;

public record RankingResponse(
        ChallengeType challengeType,
        List<RankingEntryResponse> entries
) {
    public static RankingResponse from(RankingView view) {
        return new RankingResponse(
                view.challengeType(),
                view.entries().stream().map(RankingEntryResponse::from).toList()
        );
    }
}
