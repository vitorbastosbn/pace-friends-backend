package com.pacefriends.api.profile.application;

import com.pacefriends.api.profile.domain.exception.ProfileAccessDeniedException;
import com.pacefriends.api.profile.domain.exception.UserNotFoundException;
import com.pacefriends.api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteAccountUseCase {

    private final UserRepository userRepository;

    public DeleteAccountUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void execute(UUID requestingUserId, UUID targetUserId) {
        if (!requestingUserId.equals(targetUserId)) {
            throw new ProfileAccessDeniedException();
        }
        if (!userRepository.existsById(targetUserId)) {
            throw new UserNotFoundException(targetUserId);
        }
        userRepository.deleteById(targetUserId);
    }
}
