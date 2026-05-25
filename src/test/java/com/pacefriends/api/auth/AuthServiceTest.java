package com.pacefriends.api.auth;

import com.pacefriends.api.common.exception.InvalidTokenException;
import com.pacefriends.api.common.exception.UserConflictException;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private GoogleTokenVerifierService googleTokenVerifierService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private static final String VALID_ID_TOKEN = "valid-id-token";
    private static final String GOOGLE_ID = "google-sub-123";
    private static final String EMAIL = "user@gmail.com";
    private static final String NAME = "Test User";
    private static final String PHOTO_URL = "https://photo.url";
    private static final String JWT_TOKEN = "jwt.token.here";

    private GoogleTokenInfo googleTokenInfo;

    @BeforeEach
    void setUp() {
        googleTokenInfo = new GoogleTokenInfo(GOOGLE_ID, EMAIL, NAME, PHOTO_URL);
    }

    @Test
    void googleAuth_newUser_createsUserAndReturnsToken() {
        when(googleTokenVerifierService.verify(VALID_ID_TOKEN)).thenReturn(googleTokenInfo);
        when(userRepository.findByGoogleId(GOOGLE_ID)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        User savedUser = buildUser(UUID.randomUUID());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generate(savedUser.getId(), savedUser.getName())).thenReturn(JWT_TOKEN);

        AuthResponseDTO response = authService.googleAuth(VALID_ID_TOKEN);

        assertThat(response.token()).isEqualTo(JWT_TOKEN);
        assertThat(response.user().name()).isEqualTo(NAME);
        assertThat(response.user().email()).isEqualTo(EMAIL);
        assertThat(response.user().photoUrl()).isEqualTo(PHOTO_URL);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void googleAuth_existingUser_doesNotCreateDuplicateAndReturnsToken() {
        UUID existingId = UUID.randomUUID();
        User existingUser = buildUser(existingId);

        when(googleTokenVerifierService.verify(VALID_ID_TOKEN)).thenReturn(googleTokenInfo);
        when(userRepository.findByGoogleId(GOOGLE_ID)).thenReturn(Optional.of(existingUser));
        when(jwtUtil.generate(existingId, NAME)).thenReturn(JWT_TOKEN);

        AuthResponseDTO response = authService.googleAuth(VALID_ID_TOKEN);

        assertThat(response.token()).isEqualTo(JWT_TOKEN);
        assertThat(response.user().id()).isEqualTo(existingId);

        verify(userRepository, never()).save(any());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void googleAuth_invalidToken_throwsInvalidTokenException() {
        when(googleTokenVerifierService.verify(VALID_ID_TOKEN))
                .thenThrow(new InvalidTokenException("O token fornecido e invalido ou expirou."));

        assertThatThrownBy(() -> authService.googleAuth(VALID_ID_TOKEN))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("invalido ou expirou");

        verify(userRepository, never()).findByGoogleId(any());
    }

    @Test
    void googleAuth_emailAlreadyUsedWithDifferentGoogleId_throwsUserConflictException() {
        String differentGoogleId = "different-google-id";
        GoogleTokenInfo tokenInfoWithDifferentGoogleId = new GoogleTokenInfo(
                differentGoogleId, EMAIL, NAME, PHOTO_URL);

        User userWithSameEmail = buildUser(UUID.randomUUID());

        when(googleTokenVerifierService.verify(VALID_ID_TOKEN)).thenReturn(tokenInfoWithDifferentGoogleId);
        when(userRepository.findByGoogleId(differentGoogleId)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userWithSameEmail));

        assertThatThrownBy(() -> authService.googleAuth(VALID_ID_TOKEN))
                .isInstanceOf(UserConflictException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void googleAuth_nameIsNull_usesEmailAsFallback() {
        GoogleTokenInfo tokenInfoWithNullName = new GoogleTokenInfo(GOOGLE_ID, EMAIL, null, PHOTO_URL);
        UUID newUserId = UUID.randomUUID();

        when(googleTokenVerifierService.verify(VALID_ID_TOKEN)).thenReturn(tokenInfoWithNullName);
        when(userRepository.findByGoogleId(GOOGLE_ID)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        User savedUser = buildUserWithName(newUserId, EMAIL); // name falls back to email
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generate(newUserId, EMAIL)).thenReturn(JWT_TOKEN);

        AuthResponseDTO response = authService.googleAuth(VALID_ID_TOKEN);

        assertThat(response).isNotNull();
        // Verify save was called with user having email as name
        verify(userRepository).save(argThat(user -> EMAIL.equals(user.getName())));
    }

    private User buildUser(UUID id) {
        return buildUserWithName(id, NAME);
    }

    private User buildUserWithName(UUID id, String name) {
        // Use reflection-like approach via builder; id is set by JPA, so we use a helper
        User user = User.builder()
                .googleId(GOOGLE_ID)
                .email(EMAIL)
                .name(name)
                .photoUrl(PHOTO_URL)
                .build();
        // Inject id via reflection for test purposes
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id on User for test", e);
        }
        return user;
    }
}
