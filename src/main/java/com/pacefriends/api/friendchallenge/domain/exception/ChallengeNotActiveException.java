package com.pacefriends.api.friendchallenge.domain.exception;

public class ChallengeNotActiveException extends RuntimeException {
    public ChallengeNotActiveException() {
        super("Este desafio nao esta mais ativo.");
    }
}
