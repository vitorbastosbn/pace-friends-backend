package com.pacefriends.api.challenge.application;

import com.pacefriends.api.challenge.domain.Activity;
import com.pacefriends.api.challenge.domain.ActivityRepository;
import com.pacefriends.api.challenge.domain.Challenge;
import com.pacefriends.api.challenge.domain.ChallengeProgress;
import com.pacefriends.api.challenge.domain.ChallengeRepository;
import com.pacefriends.api.challenge.domain.ChallengeStatus;
import com.pacefriends.api.challenge.domain.exception.ChallengeAccessDeniedException;
import com.pacefriends.api.challenge.domain.exception.ChallengeAlreadyCompletedException;
import com.pacefriends.api.challenge.domain.exception.ChallengeNotFoundException;
import com.pacefriends.api.challenge.event.ActivityRegisteredEvent;
import com.pacefriends.api.challenge.event.IndividualChallengeCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChallengeService {

    private static final Logger log = LoggerFactory.getLogger(ChallengeService.class);

    private final ChallengeRepository challengeRepository;
    private final ActivityRepository activityRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ChallengeService(ChallengeRepository challengeRepository, ActivityRepository activityRepository,
                             ApplicationEventPublisher eventPublisher) {
        this.challengeRepository = challengeRepository;
        this.activityRepository = activityRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Challenge createChallenge(UUID userId, String title, BigDecimal goalDistanceKm, LocalDate deadline) {
        if (deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("O prazo do desafio nao pode ser uma data passada.");
        }

        Challenge challenge = Challenge.builder()
                .userId(userId)
                .title(title)
                .goalDistanceKm(goalDistanceKm)
                .deadline(deadline)
                .status(ChallengeStatus.ACTIVE)
                .build();

        Challenge saved = challengeRepository.save(challenge);
        log.debug("Challenge created: id={}, userId={}", saved.getId(), userId);
        eventPublisher.publishEvent(new IndividualChallengeCreatedEvent(userId, saved.getId()));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ChallengeProgress> listChallenges(UUID userId) {
        List<Challenge> challenges = challengeRepository.findAllByUserId(userId);
        return challenges.stream()
                .map(this::buildProgress)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ChallengeProgress> getMyActiveChallenge(UUID userId) {
        return challengeRepository.findAllByUserId(userId).stream()
                .filter(challenge -> challenge.getStatus() == ChallengeStatus.ACTIVE)
                .findFirst()
                .map(this::buildProgress);
    }

    @Transactional(readOnly = true)
    public ChallengeProgress getChallengeDetail(UUID userId, UUID challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException(challengeId));

        validateOwnership(userId, challenge);

        return buildProgress(challenge);
    }

    @Transactional(readOnly = true)
    public ChallengeDetailView getChallengeWithActivities(UUID userId, UUID challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException(challengeId));

        validateOwnership(userId, challenge);

        ChallengeProgress progress = buildProgress(challenge);
        List<Activity> activities = activityRepository.findAllByChallengeId(challengeId);
        return new ChallengeDetailView(progress, activities);
    }

    @Transactional
    public Activity registerActivity(UUID userId, UUID challengeId, BigDecimal distanceKm,
                                     Integer durationSeconds, LocalDate activityDate, String notes) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException(challengeId));

        validateOwnership(userId, challenge);

        if (challenge.getStatus() == ChallengeStatus.COMPLETED) {
            throw new ChallengeAlreadyCompletedException(challengeId);
        }

        BigDecimal pace = BigDecimal.valueOf(durationSeconds)
                .divide(distanceKm, 2, RoundingMode.HALF_UP);

        Activity activity = Activity.builder()
                .challengeId(challengeId)
                .userId(userId)
                .distanceKm(distanceKm)
                .durationSeconds(durationSeconds)
                .paceSecondsPerKm(pace)
                .activityDate(activityDate)
                .notes(notes)
                .build();

        Activity saved = activityRepository.save(activity);
        log.debug("Activity registered: id={}, challengeId={}, distanceKm={}", saved.getId(), challengeId, distanceKm);
        eventPublisher.publishEvent(new ActivityRegisteredEvent(userId, challengeId));

        updateChallengeStatusIfCompleted(challenge);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Activity> listActivities(UUID userId, UUID challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException(challengeId));

        validateOwnership(userId, challenge);

        return activityRepository.findAllByChallengeId(challengeId);
    }

    private void validateOwnership(UUID userId, Challenge challenge) {
        if (!userId.equals(challenge.getUserId())) {
            throw new ChallengeAccessDeniedException();
        }
    }

    private ChallengeProgress buildProgress(Challenge challenge) {
        BigDecimal progressKm = activityRepository.sumDistanceByChallengeId(challenge.getId());
        if (progressKm == null) {
            progressKm = BigDecimal.ZERO;
        }

        BigDecimal progressPct = BigDecimal.ZERO;
        if (challenge.getGoalDistanceKm().compareTo(BigDecimal.ZERO) > 0) {
            progressPct = progressKm
                    .divide(challenge.getGoalDistanceKm(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new ChallengeProgress(challenge, progressKm, progressPct);
    }

    private void updateChallengeStatusIfCompleted(Challenge challenge) {
        BigDecimal progressKm = activityRepository.sumDistanceByChallengeId(challenge.getId());
        if (progressKm == null) {
            progressKm = BigDecimal.ZERO;
        }

        if (progressKm.compareTo(challenge.getGoalDistanceKm()) >= 0) {
            Challenge completed = Challenge.builder()
                    .id(challenge.getId())
                    .userId(challenge.getUserId())
                    .title(challenge.getTitle())
                    .goalDistanceKm(challenge.getGoalDistanceKm())
                    .deadline(challenge.getDeadline())
                    .status(ChallengeStatus.COMPLETED)
                    .createdAt(challenge.getCreatedAt())
                    .build();
            challengeRepository.save(completed);
            log.debug("Challenge completed: id={}", challenge.getId());
        }
    }
}
