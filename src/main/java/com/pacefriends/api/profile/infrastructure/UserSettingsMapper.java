package com.pacefriends.api.profile.infrastructure;

import com.pacefriends.api.profile.domain.UserObjective;
import com.pacefriends.api.profile.domain.UserSettings;
import com.pacefriends.api.profile.domain.WeeklyFrequency;

class UserSettingsMapper {

    private UserSettingsMapper() {
    }

    static UserSettings toDomain(UserSettingsEntity entity) {
        return UserSettings.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .objective(UserObjective.valueOf(entity.getObjective()))
                .weeklyFrequency(WeeklyFrequency.valueOf(entity.getWeeklyFrequency()))
                .effectiveFrom(entity.getEffectiveFrom())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
