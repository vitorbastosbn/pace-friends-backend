package com.pacefriends.api.friendchallenge.domain.exception;

public class ChallengeFullException extends RuntimeException {
    public ChallengeFullException() {
        super("Este desafio ja atingiu o limite de 5 participantes.");
    }
}
