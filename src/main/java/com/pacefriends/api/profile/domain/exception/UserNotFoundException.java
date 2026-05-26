package com.pacefriends.api.profile.domain.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID userId) {
        super("Usuario nao encontrado: " + userId);
    }
}
