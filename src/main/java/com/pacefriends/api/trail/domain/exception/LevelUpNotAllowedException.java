package com.pacefriends.api.trail.domain.exception;

public class LevelUpNotAllowedException extends RuntimeException {

    public LevelUpNotAllowedException(String reason) {
        super(reason);
    }
}
