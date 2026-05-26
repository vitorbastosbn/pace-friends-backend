package com.pacefriends.api.profile.domain.exception;

public class ProfileAccessDeniedException extends RuntimeException {

    public ProfileAccessDeniedException() {
        super("Acesso negado: voce so pode acessar seu proprio perfil.");
    }
}
