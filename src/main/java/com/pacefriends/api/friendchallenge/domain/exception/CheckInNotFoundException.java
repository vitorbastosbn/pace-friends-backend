package com.pacefriends.api.friendchallenge.domain.exception;

public class CheckInNotFoundException extends RuntimeException {
    public CheckInNotFoundException() {
        super("Check-in nao encontrado.");
    }
}
