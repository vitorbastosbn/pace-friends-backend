package com.pacefriends.api.friendchallenge.application;

import com.pacefriends.api.friendchallenge.domain.FriendChallengeCheckIn;

public record CheckInWithUserName(
        FriendChallengeCheckIn checkIn,
        String userName
) {
}
