package com.pacefriends.api.achievement.application;

import com.pacefriends.api.challenge.event.IndividualChallengeCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ChallengeAchievementListener {

    private static final Logger log = LoggerFactory.getLogger(ChallengeAchievementListener.class);
    private static final String SLUG_CREATOR = "criador-de-desafios";

    private final AchievementUnlockService unlockService;

    public ChallengeAchievementListener(AchievementUnlockService unlockService) {
        this.unlockService = unlockService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onChallengeCreated(IndividualChallengeCreatedEvent event) {
        try {
            unlockService.tryUnlock(event.userId(), SLUG_CREATOR);
        } catch (Exception e) {
            log.error("Achievement evaluation failed for challenge created event userId={}", event.userId(), e);
        }
    }
}
