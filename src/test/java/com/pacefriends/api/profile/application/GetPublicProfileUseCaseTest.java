package com.pacefriends.api.profile.application;

import com.pacefriends.api.achievement.infrastructure.UserAchievementJpaRepository;
import com.pacefriends.api.friendchallenge.infrastructure.FriendChallengeParticipantJpaRepository;
import com.pacefriends.api.profile.domain.exception.UserNotFoundException;
import com.pacefriends.api.profile.presentation.PublicProfileResponse;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPublicProfileUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendChallengeParticipantJpaRepository participantRepository;

    @Mock
    private UserAchievementJpaRepository userAchievementRepository;

    @InjectMocks
    private GetPublicProfileUseCase useCase;

    @Test
    void execute_userFound_returnsPublicProfile() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "Ana Lima", "https://photo.url");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(participantRepository.countVictoriesForUser(userId)).thenReturn(3);
        when(userAchievementRepository.countByUserId(userId)).thenReturn(5);

        PublicProfileResponse result = useCase.execute(userId);

        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.name()).isEqualTo("Ana Lima");
        assertThat(result.avatarUrl()).isEqualTo("https://photo.url");
        assertThat(result.totalVictories()).isEqualTo(3);
        assertThat(result.achievementsUnlocked()).isEqualTo(5);
    }

    @Test
    void execute_userNotFound_throwsUserNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    private User buildUser(UUID id, String name, String photoUrl) {
        User u = User.builder()
                .googleId("google-id")
                .email("user@example.com")
                .name(name)
                .photoUrl(photoUrl)
                .build();
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(u, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return u;
    }
}
