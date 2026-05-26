package com.pacefriends.api.friendchallenge.domain.exception;

public class FriendChallengeAccessDeniedException extends RuntimeException {
    public FriendChallengeAccessDeniedException() {
        super("Acesso negado. Voce nao e participante deste desafio.");
    }
}
