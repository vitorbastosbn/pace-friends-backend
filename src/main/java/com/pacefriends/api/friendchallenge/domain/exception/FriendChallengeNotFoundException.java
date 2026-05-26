package com.pacefriends.api.friendchallenge.domain.exception;

import java.util.UUID;

public class FriendChallengeNotFoundException extends RuntimeException {
    public FriendChallengeNotFoundException(UUID id) {
        super("Desafio nao encontrado: " + id);
    }
}
