package com.pacefriends.api.profile.application;

import com.pacefriends.api.profile.domain.exception.ProfileAccessDeniedException;
import com.pacefriends.api.profile.domain.exception.UserNotFoundException;
import com.pacefriends.api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteAccountUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeleteAccountUseCase useCase;

    @Test
    void execute_ownAccount_deletesUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);

        useCase.execute(userId, userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void execute_differentUser_throwsProfileAccessDeniedException() {
        UUID requestingUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(requestingUserId, targetUserId))
                .isInstanceOf(ProfileAccessDeniedException.class);

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void execute_userNotFound_throwsUserNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(userId, userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }
}
