package com.pacefriends.api.challenge.presentation;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateChallengeRequest(
        @NotBlank(message = "O titulo e obrigatorio.")
        @Size(min = 3, max = 100, message = "O titulo deve ter entre 3 e 100 caracteres.")
        String title,

        @NotNull(message = "A meta de distancia e obrigatoria.")
        @Positive(message = "A meta de distancia deve ser maior que zero.")
        BigDecimal goalDistanceKm,

        @NotNull(message = "O prazo e obrigatorio.")
        @FutureOrPresent(message = "O prazo nao pode ser uma data passada.")
        LocalDate deadline
) {
}
