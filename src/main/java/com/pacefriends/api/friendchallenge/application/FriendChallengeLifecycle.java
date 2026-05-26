package com.pacefriends.api.friendchallenge.application;

import com.pacefriends.api.friendchallenge.domain.FriendChallenge;
import com.pacefriends.api.friendchallenge.domain.FriendChallengeRepository;

import java.time.LocalDate;

final class FriendChallengeLifecycle {

    private FriendChallengeLifecycle() {
    }

    static FriendChallenge applyTransitionIfNeeded(FriendChallenge challenge,
                                                   FriendChallengeRepository repository,
                                                   LocalDate today) {
        if (!FriendChallenge.STATUS_ACTIVE.equals(challenge.status())
                && !FriendChallenge.STATUS_AUDIT.equals(challenge.status())) {
            return challenge;
        }

        String nextStatus = challenge.status();
        if (today.isAfter(challenge.endDate())) {
            nextStatus = FriendChallenge.STATUS_FINISHED;
        } else if (today.isEqual(challenge.endDate())) {
            nextStatus = FriendChallenge.STATUS_AUDIT;
        }

        if (!nextStatus.equals(challenge.status())) {
            repository.updateStatus(challenge.id(), nextStatus);
            return challenge.withStatus(nextStatus);
        }
        return challenge;
    }
}
