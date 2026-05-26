package com.pacefriends.api.challenge.domain.exception;

import java.util.UUID;

public class ChallengeAlreadyCompletedException extends RuntimeException {

    public ChallengeAlreadyCompletedException(UUID challengeId) {
        super("Desafio ja concluido: " + challengeId);
    }
}
