package com.pacefriends.api.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequest(
        @NotBlank(message = "O campo idToken e obrigatorio.")
        String idToken
) {
}
