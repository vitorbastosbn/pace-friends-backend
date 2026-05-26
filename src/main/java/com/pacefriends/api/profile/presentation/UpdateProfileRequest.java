package com.pacefriends.api.profile.presentation;

import com.pacefriends.api.profile.domain.UserObjective;
import com.pacefriends.api.profile.domain.WeeklyFrequency;
import jakarta.validation.constraints.NotNull;

public record UpdateProfileRequest(
        @NotNull(message = "objective e obrigatorio.")
        UserObjective objective,

        @NotNull(message = "weeklyFrequency e obrigatorio.")
        WeeklyFrequency weeklyFrequency
) {
}
