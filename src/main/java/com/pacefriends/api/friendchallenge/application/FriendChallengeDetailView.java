package com.pacefriends.api.friendchallenge.application;

import com.pacefriends.api.friendchallenge.domain.FriendChallenge;
import com.pacefriends.api.friendchallenge.domain.FriendChallengeParticipant;

import java.util.List;

public record FriendChallengeDetailView(
        FriendChallenge challenge,
        List<FriendChallengeParticipant> participants
) {
}
