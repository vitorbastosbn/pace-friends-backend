package com.pacefriends.api.friendchallenge.domain.exception;

public class CheckInAuditDateException extends RuntimeException {
    public CheckInAuditDateException() {
        super("Registro de check-in nao permitido no dia de auditoria.");
    }
}
