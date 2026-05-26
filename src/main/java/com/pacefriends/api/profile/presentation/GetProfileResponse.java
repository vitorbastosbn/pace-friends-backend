package com.pacefriends.api.profile.presentation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pacefriends.api.profile.domain.ProfileData;

import java.time.LocalDate;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetProfileResponse(
        UUID id,
        String name,
        String email,
        String photoUrl,
        String objective,
        String weeklyFrequency,
        LocalDate effectiveFrom,
        StatsDTO stats
) {

    public record StatsDTO(int totalXp, int currentStreak, int achievementsUnlocked) {
    }

    public static GetProfileResponse from(ProfileData data) {
        return new GetProfileResponse(
                data.getUserId(),
                data.getName(),
                data.getEmail(),
                data.getPhotoUrl(),
                data.getObjective().name(),
                data.getWeeklyFrequency().name(),
                data.getEffectiveFrom(),
                new StatsDTO(
                        data.getStats().getTotalXp(),
                        data.getStats().getCurrentStreak(),
                        data.getStats().getAchievementsUnlocked()
                )
        );
    }
}
