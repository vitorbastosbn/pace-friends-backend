package com.pacefriends.api.friendchallenge.infrastructure;

import com.pacefriends.api.friendchallenge.domain.ChallengeType;
import com.pacefriends.api.friendchallenge.domain.FriendChallenge;
import com.pacefriends.api.friendchallenge.domain.ParticipantRole;

import java.util.List;

class FriendChallengeMapper {

    private FriendChallengeMapper() {
    }

    static FriendChallenge toDomain(FriendChallengeEntity entity, int participantCount,
                                     ParticipantRole myRole) {
        return new FriendChallenge(
                entity.getId(),
                entity.getCreatorId(),
                entity.getTitle(),
                entity.getDescription(),
                ChallengeType.valueOf(entity.getChallengeType()),
                entity.getGoalValue(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getInviteCode(),
                entity.getStatus(),
                participantCount,
                5,
                myRole,
                entity.getCreatedAt(),
                List.of()
        );
    }

    static FriendChallengeEntity toEntity(FriendChallenge domain) {
        FriendChallengeEntity entity = new FriendChallengeEntity(
                domain.creatorId(),
                domain.title(),
                domain.description(),
                domain.challengeType().name(),
                domain.goalValue(),
                domain.startDate(),
                domain.endDate(),
                domain.inviteCode(),
                domain.status()
        );
        if (domain.id() != null) {
            entity.setId(domain.id());
        }
        if (domain.createdAt() != null) {
            entity.setCreatedAt(domain.createdAt());
        }
        return entity;
    }
}
