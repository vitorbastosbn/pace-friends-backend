package com.pacefriends.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pacefriends.api.common.exception.InvalidTokenException;
import com.pacefriends.api.common.exception.UserConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.pacefriends.api.auth.JwtUtil;
import com.pacefriends.api.config.JacksonConfig;
import com.pacefriends.api.config.SecurityConfig;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JacksonConfig.class, JwtUtil.class})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.pacefriends.api.user.UserRepository userRepository;

    @Test
    void googleAuth_validRequest_returns200WithTokenAndUser() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthResponseDTO response = new AuthResponseDTO(
                "jwt.token.here",
                new AuthResponseDTO.UserDTO(userId, "Test User", "user@gmail.com", "https://photo.url")
        );

        when(authService.googleAuth("valid-id-token")).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "idToken": "valid-id-token" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.user.id").value(userId.toString()))
                .andExpect(jsonPath("$.user.name").value("Test User"))
                .andExpect(jsonPath("$.user.email").value("user@gmail.com"))
                .andExpect(jsonPath("$.user.photoUrl").value("https://photo.url"));
    }

    @Test
    void googleAuth_invalidGoogleToken_returns401() throws Exception {
        when(authService.googleAuth("invalid-token"))
                .thenThrow(new InvalidTokenException("O token fornecido e invalido ou expirou."));

        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "idToken": "invalid-token" }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_token"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void googleAuth_missingIdToken_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void googleAuth_blankIdToken_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "idToken": "" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"));
    }

    @Test
    void googleAuth_malformedBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void googleAuth_userConflict_returns409() throws Exception {
        when(authService.googleAuth("conflicting-token"))
                .thenThrow(new UserConflictException("E-mail ja associado a outra conta."));

        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "idToken": "conflicting-token" }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("user_conflict"));
    }
}
