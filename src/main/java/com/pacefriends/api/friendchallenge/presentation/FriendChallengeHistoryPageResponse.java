package com.pacefriends.api.friendchallenge.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pacefriends.api.friendchallenge.application.FriendChallengeService.HistoryPage;
import com.pacefriends.api.friendchallenge.domain.FriendChallenge;

import java.util.List;
import java.util.function.Function;

public record FriendChallengeHistoryPageResponse(
        List<FriendChallengeListItemResponse> content,
        int page,
        int size,
        @JsonProperty("total_elements") long totalElements,
        @JsonProperty("has_next") boolean hasNext
) {
    public static FriendChallengeHistoryPageResponse from(
            HistoryPage historyPage, int page, int size,
            Function<FriendChallenge, Integer> rankResolver) {
        List<FriendChallengeListItemResponse> content = historyPage.challenges().stream()
                .map(c -> FriendChallengeListItemResponse.from(c, rankResolver.apply(c)))
                .toList();
        return new FriendChallengeHistoryPageResponse(
                content, page, size, historyPage.totalElements(), historyPage.hasNext()
        );
    }
}
