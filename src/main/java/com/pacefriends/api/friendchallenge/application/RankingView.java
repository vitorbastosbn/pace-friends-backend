package com.pacefriends.api.friendchallenge.application;

import com.pacefriends.api.friendchallenge.domain.ChallengeType;
import com.pacefriends.api.friendchallenge.domain.RankingEntry;

import java.util.List;

public record RankingView(
        ChallengeType challengeType,
        List<RankingEntry> entries
) {
}
