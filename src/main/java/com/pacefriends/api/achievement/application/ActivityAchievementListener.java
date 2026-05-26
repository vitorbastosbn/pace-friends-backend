package com.pacefriends.api.achievement.application;

import com.pacefriends.api.achievement.infrastructure.AchievementJpaRepository;
import com.pacefriends.api.challenge.domain.ActivityRepository;
import com.pacefriends.api.challenge.event.ActivityRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ActivityAchievementListener {

    private static final Logger log = LoggerFactory.getLogger(ActivityAchievementListener.class);
    private static final String SLUG_FIRST_ACTIVITY = "primeiro-passo";

    private final AchievementUnlockService unlockService;
    private final ActivityRepository activityRepository;
    private final AchievementJpaRepository achievementRepository;

    public ActivityAchievementListener(AchievementUnlockService unlockService,
                                        ActivityRepository activityRepository,
                                        AchievementJpaRepository achievementRepository) {
        this.unlockService = unlockService;
        this.activityRepository = activityRepository;
        this.achievementRepository = achievementRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onActivityRegistered(ActivityRegisteredEvent event) {
        try {
            long totalActivities = activityRepository.countByUserId(event.userId());

            if (totalActivities == 1) {
                unlockService.tryUnlock(event.userId(), SLUG_FIRST_ACTIVITY);
            }

            achievementRepository.findByCriteriaType("ACTIVITIES_COUNT").forEach(achievement -> {
                if (achievement.getCriteriaValue() != null
                        && totalActivities == achievement.getCriteriaValue().longValue()) {
                    unlockService.tryUnlock(event.userId(), achievement.getSlug());
                }
            });
        } catch (Exception e) {
            log.error("Achievement evaluation failed for activity event userId={}", event.userId(), e);
        }
    }
}
