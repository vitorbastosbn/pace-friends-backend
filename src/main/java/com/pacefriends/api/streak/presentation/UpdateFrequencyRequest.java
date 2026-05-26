package com.pacefriends.api.streak.presentation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateFrequencyRequest(
        @Min(value = 1, message = "weeklyFrequency must be between 1 and 7")
        @Max(value = 7, message = "weeklyFrequency must be between 1 and 7")
        int weeklyFrequency
) {
}
