package com.pacefriends.api.achievement.application;

import com.pacefriends.api.friendchallenge.event.FriendChallengeFinishedEvent;
import com.pacefriends.api.friendchallenge.infrastructure.FriendChallengeCheckInEntity;
import com.pacefriends.api.friendchallenge.infrastructure.FriendChallengeCheckInJpaRepository;
import com.pacefriends.api.friendchallenge.infrastructure.FriendChallengeParticipantJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FriendChallengeAchievementListener {

    private static final Logger log = LoggerFactory.getLogger(FriendChallengeAchievementListener.class);
    private static final String SLUG_COMPETITOR = "competidor";
    private static final String SLUG_FIRST_VICTORY = "primeira-vitoria";
    private static final String STATUS_VALID = "VALID";

    private final AchievementUnlockService unlockService;
    private final FriendChallengeParticipantJpaRepository participantRepository;
    private final FriendChallengeCheckInJpaRepository checkInRepository;

    public FriendChallengeAchievementListener(AchievementUnlockService unlockService,
                                               FriendChallengeParticipantJpaRepository participantRepository,
                                               FriendChallengeCheckInJpaRepository checkInRepository) {
        this.unlockService = unlockService;
        this.participantRepository = participantRepository;
        this.checkInRepository = checkInRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onFriendChallengeFinished(FriendChallengeFinishedEvent event) {
        try {
            UUID challengeId = event.challengeId();

            List<UUID> participantIds = participantRepository
                    .findAllByFriendChallengeId(challengeId)
                    .stream()
                    .map(p -> p.getUserId())
                    .toList();

            participantIds.forEach(userId -> unlockService.tryUnlock(userId, SLUG_COMPETITOR));

            List<FriendChallengeCheckInEntity> validCheckIns = checkInRepository
                    .findAllByFriendChallengeId(challengeId)
                    .stream()
                    .filter(ci -> STATUS_VALID.equals(ci.getStatus()))
                    .toList();

            if (validCheckIns.isEmpty()) {
                return;
            }

            Map<UUID, Double> distanceByUser = validCheckIns.stream()
                    .collect(Collectors.groupingBy(
                            FriendChallengeCheckInEntity::getUserId,
                            Collectors.summingDouble(FriendChallengeCheckInEntity::getDistanceKm)
                    ));

            double maxDistance = distanceByUser.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0.0);

            distanceByUser.entrySet().stream()
                    .filter(e -> e.getValue() >= maxDistance)
                    .map(Map.Entry::getKey)
                    .forEach(userId -> unlockService.tryUnlock(userId, SLUG_FIRST_VICTORY));

        } catch (Exception e) {
            log.error("Achievement evaluation failed for friend challenge finished event challengeId={}",
                    event.challengeId(), e);
        }
    }
}
