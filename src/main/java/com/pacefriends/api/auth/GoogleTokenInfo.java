package com.pacefriends.api.auth;

public record GoogleTokenInfo(
        String googleId,
        String email,
        String name,
        String picture
) {
}
