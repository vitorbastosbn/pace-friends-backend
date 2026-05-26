package com.pacefriends.api.friendchallenge.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pacefriends.api.friendchallenge.domain.FriendChallenge;
import com.pacefriends.api.friendchallenge.domain.ParticipantRole;

public record ChallengePermissionsResponse(
        @JsonProperty("can_check_in") boolean canCheckIn,
        @JsonProperty("can_leave") boolean canLeave,
        @JsonProperty("can_delete") boolean canDelete,
        @JsonProperty("can_reject_check_ins") boolean canRejectCheckIns
) {
    public static ChallengePermissionsResponse from(FriendChallenge challenge) {
        boolean active = FriendChallenge.STATUS_ACTIVE.equals(challenge.status());
        boolean audit = FriendChallenge.STATUS_AUDIT.equals(challenge.status());
        boolean member = ParticipantRole.MEMBER.equals(challenge.myRole());
        boolean creator = ParticipantRole.CREATOR.equals(challenge.myRole());
        return new ChallengePermissionsResponse(
                member && active,
                member && active,
                creator && (active || audit),
                creator && audit
        );
    }
}
