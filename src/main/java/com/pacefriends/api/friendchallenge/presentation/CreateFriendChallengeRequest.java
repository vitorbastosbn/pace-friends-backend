package com.pacefriends.api.friendchallenge.presentation;

import com.pacefriends.api.friendchallenge.domain.ChallengeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateFriendChallengeRequest(
        @NotBlank(message = "Titulo e obrigatorio.")
        @Size(min = 3, max = 100, message = "Titulo deve ter entre 3 e 100 caracteres.")
        String title,

        @Size(max = 500, message = "Descricao deve ter no maximo 500 caracteres.")
        String description,

        @NotNull(message = "Tipo de desafio e obrigatorio.")
        ChallengeType challengeType,

        BigDecimal goalValue,

        @NotNull(message = "Data de inicio e obrigatoria.")
        LocalDate startDate,

        @NotNull(message = "Data de fim e obrigatoria.")
        LocalDate endDate
) {
}
