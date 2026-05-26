package com.pacefriends.api.challenge.infrastructure;

import com.pacefriends.api.challenge.domain.Activity;

class ActivityMapper {

    private ActivityMapper() {
    }

    static Activity toDomain(ActivityEntity entity) {
        return Activity.builder()
                .id(entity.getId())
                .challengeId(entity.getChallengeId())
                .userId(entity.getUserId())
                .distanceKm(entity.getDistanceKm())
                .durationSeconds(entity.getDurationSeconds())
                .paceSecondsPerKm(entity.getPaceSecondsPerKm())
                .activityDate(entity.getActivityDate())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    static ActivityEntity toEntity(Activity domain) {
        return new ActivityEntity(
                domain.getChallengeId(),
                domain.getUserId(),
                domain.getDistanceKm(),
                domain.getDurationSeconds(),
                domain.getPaceSecondsPerKm(),
                domain.getActivityDate(),
                domain.getNotes()
        );
    }
}
