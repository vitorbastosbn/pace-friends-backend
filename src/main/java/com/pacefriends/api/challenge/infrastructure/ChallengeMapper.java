package com.pacefriends.api.challenge.infrastructure;

import com.pacefriends.api.challenge.domain.Challenge;
import com.pacefriends.api.challenge.domain.ChallengeStatus;

class ChallengeMapper {

    private ChallengeMapper() {
    }

    static Challenge toDomain(ChallengeEntity entity) {
        return Challenge.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .goalDistanceKm(entity.getGoalDistanceKm())
                .deadline(entity.getDeadline())
                .status(ChallengeStatus.valueOf(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    static ChallengeEntity toEntity(Challenge domain) {
        ChallengeEntity entity = new ChallengeEntity(
                domain.getUserId(),
                domain.getTitle(),
                domain.getGoalDistanceKm(),
                domain.getDeadline(),
                domain.getStatus().name()
        );
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        if (domain.getCreatedAt() != null) {
            entity.setCreatedAt(domain.getCreatedAt());
        }
        return entity;
    }
}
