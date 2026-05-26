package com.pacefriends.api.friendchallenge.domain.exception;

public class CheckInAlreadyRejectedException extends RuntimeException {
    public CheckInAlreadyRejectedException() {
        super("Este check-in ja foi rejeitado.");
    }
}
