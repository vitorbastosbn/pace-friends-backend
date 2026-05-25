package com.pacefriends.api.auth;

import java.util.UUID;

public record AuthResponseDTO(String token, UserDTO user) {

    public record UserDTO(UUID id, String name, String email, String photoUrl) {
    }
}
