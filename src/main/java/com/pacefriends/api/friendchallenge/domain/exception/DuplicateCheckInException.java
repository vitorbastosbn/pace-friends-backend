package com.pacefriends.api.friendchallenge.domain.exception;

public class DuplicateCheckInException extends RuntimeException {
    public DuplicateCheckInException() {
        super("Voce ja registrou um check-in neste dia.");
    }
}
