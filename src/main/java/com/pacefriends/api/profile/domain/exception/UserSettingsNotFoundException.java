package com.pacefriends.api.profile.domain.exception;

import java.util.UUID;

public class UserSettingsNotFoundException extends RuntimeException {

    public UserSettingsNotFoundException(UUID userId) {
        super("Configuracoes de perfil nao encontradas para o usuario: " + userId);
    }
}
