package com.pacefriends.api.challenge.domain.exception;

public class ChallengeAccessDeniedException extends RuntimeException {

    public ChallengeAccessDeniedException() {
        super("Acesso negado: voce so pode acessar seus proprios desafios.");
    }
}
