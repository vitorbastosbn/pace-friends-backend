package com.pacefriends.api.friendchallenge.domain.exception;

public class InvalidInviteCodeException extends RuntimeException {
    public InvalidInviteCodeException() {
        super("Codigo de convite invalido.");
    }
}
