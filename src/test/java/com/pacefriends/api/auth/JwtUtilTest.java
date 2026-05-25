package com.pacefriends.api.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET = "test-secret-key-pace-friends-long-enough";
    private static final long EXPIRATION_SECONDS = 86400L;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, EXPIRATION_SECONDS);
    }

    @Test
    void generate_returnsNonBlankToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generate(userId, "Test User");

        assertThat(token).isNotBlank();
    }

    @Test
    void generate_tokenContainsUserId() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generate(userId, "Test User");

        DecodedJWT decoded = jwtUtil.decode(token);
        assertThat(decoded.getClaim("userId").asString()).isEqualTo(userId.toString());
    }

    @Test
    void generate_tokenContainsName() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generate(userId, "Test User");

        DecodedJWT decoded = jwtUtil.decode(token);
        assertThat(decoded.getClaim("name").asString()).isEqualTo("Test User");
    }

    @Test
    void generate_tokenHasExpiration() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generate(userId, "Test User");

        DecodedJWT decoded = jwtUtil.decode(token);
        assertThat(decoded.getExpiresAt()).isNotNull();
    }

    @Test
    void generate_tokenDoesNotContainSensitiveFields() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generate(userId, "Test User");

        DecodedJWT decoded = jwtUtil.decode(token);
        assertThat(decoded.getClaim("email").asString()).isNull();
        assertThat(decoded.getClaim("photoUrl").asString()).isNull();
    }

    @Test
    void decode_invalidToken_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> jwtUtil.decode("invalid.token.here"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void decode_validToken_returnsDecodedJwt() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generate(userId, "Test User");

        DecodedJWT decoded = jwtUtil.decode(token);

        assertThat(decoded).isNotNull();
        assertThat(decoded.getClaim("userId").asString()).isEqualTo(userId.toString());
    }

    @Test
    void generate_expiredTokenIsRejectedByDecode() {
        JwtUtil shortLivedUtil = new JwtUtil(SECRET, -1L); // negative expiration
        UUID userId = UUID.randomUUID();
        String token = shortLivedUtil.generate(userId, "Test User");

        assertThatThrownBy(() -> jwtUtil.decode(token))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
