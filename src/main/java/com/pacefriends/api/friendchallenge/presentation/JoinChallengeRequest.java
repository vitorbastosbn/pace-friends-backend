package com.pacefriends.api.friendchallenge.presentation;

import jakarta.validation.constraints.NotBlank;

public record JoinChallengeRequest(
        @NotBlank(message = "Codigo de convite e obrigatorio.")
        String inviteCode
) {
}
