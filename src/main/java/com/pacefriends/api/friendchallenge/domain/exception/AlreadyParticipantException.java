package com.pacefriends.api.friendchallenge.domain.exception;

public class AlreadyParticipantException extends RuntimeException {
    public AlreadyParticipantException() {
        super("Voce ja e participante deste desafio.");
    }
}
