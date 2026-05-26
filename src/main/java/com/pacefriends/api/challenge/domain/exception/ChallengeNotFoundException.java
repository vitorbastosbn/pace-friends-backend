package com.pacefriends.api.challenge.domain.exception;

import java.util.UUID;

public class ChallengeNotFoundException extends RuntimeException {

    public ChallengeNotFoundException(UUID challengeId) {
        super("Desafio nao encontrado: " + challengeId);
    }
}
