package com.pacefriends.api.friendchallenge.domain.exception;

public class ChallengeNotInAuditException extends RuntimeException {
    public ChallengeNotInAuditException() {
        super("O desafio nao esta em periodo de auditoria.");
    }
}
