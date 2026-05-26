package com.pacefriends.api.streak.infrastructure;

import com.pacefriends.api.streak.domain.StreakResult;
import com.pacefriends.api.streak.domain.WeeklyStreak;

class WeeklyStreakMapper {

    private WeeklyStreakMapper() {
    }

    static WeeklyStreakEntity toEntity(WeeklyStreak domain) {
        return new WeeklyStreakEntity(
                domain.getUserId(),
                domain.getWeekStartDate(),
                domain.getTargetFrequency(),
                domain.getDaysCompleted(),
                domain.getStreakCount(),
                domain.getXpEarned(),
                domain.getResult().name(),
                domain.getProcessedAt()
        );
    }

    static WeeklyStreak toDomain(WeeklyStreakEntity entity) {
        return WeeklyStreak.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .weekStartDate(entity.getWeekStartDate())
                .targetFrequency(entity.getTargetFrequency())
                .daysCompleted(entity.getDaysCompleted())
                .streakCount(entity.getStreakCount())
                .xpEarned(entity.getXpEarned())
                .result(StreakResult.valueOf(entity.getResult()))
                .processedAt(entity.getProcessedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
