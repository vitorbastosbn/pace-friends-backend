package com.pacefriends.api.trail.domain.exception;

public class TrailAccessDeniedException extends RuntimeException {

    public TrailAccessDeniedException() {
        super("Acesso negado ao recurso de trilha.");
    }
}
