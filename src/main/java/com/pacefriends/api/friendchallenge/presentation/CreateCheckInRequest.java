package com.pacefriends.api.friendchallenge.presentation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateCheckInRequest(
        @NotNull(message = "distanceKm e obrigatorio.")
        @Positive(message = "distanceKm deve ser maior que zero.")
        Double distanceKm,

        @NotNull(message = "durationSeconds e obrigatorio.")
        @Positive(message = "durationSeconds deve ser maior que zero.")
        Integer durationSeconds,

        @NotNull(message = "checkInDate e obrigatoria.")
        LocalDate checkInDate,

        String notes
) {
}
