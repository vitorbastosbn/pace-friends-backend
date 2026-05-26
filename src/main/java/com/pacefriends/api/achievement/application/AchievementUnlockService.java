package com.pacefriends.api.achievement.application;

import com.pacefriends.api.achievement.infrastructure.AchievementJpaRepository;
import com.pacefriends.api.achievement.infrastructure.UserAchievementJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AchievementUnlockService {

    private static final Logger log = LoggerFactory.getLogger(AchievementUnlockService.class);

    private final AchievementJpaRepository achievementRepository;
    private final UserAchievementJpaRepository userAchievementRepository;

    public AchievementUnlockService(AchievementJpaRepository achievementRepository,
                                     UserAchievementJpaRepository userAchievementRepository) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
    }

    @Transactional
    public void tryUnlock(UUID userId, String slug) {
        achievementRepository.findBySlug(slug).ifPresentOrElse(
                achievement -> {
                    userAchievementRepository.insertIfNotExists(userId, achievement.getId());
                    log.debug("Achievement '{}' unlock attempted for user {}", slug, userId);
                },
                () -> log.warn("Achievement with slug '{}' not found in catalog", slug)
        );
    }
}
