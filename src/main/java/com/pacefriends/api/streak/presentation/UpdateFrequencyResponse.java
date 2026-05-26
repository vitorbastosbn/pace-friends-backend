package com.pacefriends.api.streak.presentation;

import com.pacefriends.api.streak.application.UpdateWeeklyFrequencyService;

import java.time.LocalDate;

public record UpdateFrequencyResponse(
        int weeklyFrequency,
        LocalDate effectiveFrom
) {
    public static UpdateFrequencyResponse from(UpdateWeeklyFrequencyService.FrequencyUpdate update) {
        return new UpdateFrequencyResponse(update.weeklyFrequency(), update.effectiveFrom());
    }
}
