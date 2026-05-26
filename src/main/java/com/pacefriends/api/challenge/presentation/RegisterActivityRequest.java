package com.pacefriends.api.challenge.presentation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterActivityRequest(
        @NotNull(message = "A distancia e obrigatoria.")
        @Positive(message = "A distancia deve ser maior que zero.")
        BigDecimal distanceKm,

        @NotNull(message = "A duracao e obrigatoria.")
        @Positive(message = "A duracao deve ser maior que zero.")
        Integer durationSeconds,

        @NotNull(message = "A data da atividade e obrigatoria.")
        @PastOrPresent(message = "A data da atividade nao pode ser futura.")
        LocalDate activityDate,

        String notes
) {
}
