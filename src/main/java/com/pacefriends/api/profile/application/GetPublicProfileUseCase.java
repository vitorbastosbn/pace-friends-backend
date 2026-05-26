package com.pacefriends.api.profile.application;

import com.pacefriends.api.achievement.infrastructure.UserAchievementJpaRepository;
import com.pacefriends.api.friendchallenge.infrastructure.FriendChallengeParticipantJpaRepository;
import com.pacefriends.api.profile.domain.exception.UserNotFoundException;
import com.pacefriends.api.profile.presentation.PublicProfileResponse;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetPublicProfileUseCase {

    private final UserRepository userRepository;
    private final FriendChallengeParticipantJpaRepository participantRepository;
    private final UserAchievementJpaRepository userAchievementRepository;

    public GetPublicProfileUseCase(UserRepository userRepository,
                                   FriendChallengeParticipantJpaRepository participantRepository,
                                   UserAchievementJpaRepository userAchievementRepository) {
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.userAchievementRepository = userAchievementRepository;
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse execute(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        int totalVictories = participantRepository.countVictoriesForUser(userId);
        int achievementsUnlocked = userAchievementRepository.countByUserId(userId);

        return new PublicProfileResponse(
                user.getId(),
                user.getName(),
                user.getPhotoUrl(),
                totalVictories,
                achievementsUnlocked
        );
    }
}
