package com.pacefriends.api.friendchallenge.application;

import com.pacefriends.api.friendchallenge.domain.*;
import com.pacefriends.api.friendchallenge.domain.exception.*;
import com.pacefriends.api.friendchallenge.infrastructure.FriendChallengeParticipantJpaRepository;
import com.pacefriends.api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckInService {

    private final FriendChallengeRepository challengeRepository;
    private final FriendChallengeCheckInRepository checkInRepository;
    private final FriendChallengeParticipantJpaRepository participantJpaRepository;
    private final UserRepository userRepository;

    public CheckInService(FriendChallengeRepository challengeRepository,
                          FriendChallengeCheckInRepository checkInRepository,
                          FriendChallengeParticipantJpaRepository participantJpaRepository,
                          UserRepository userRepository) {
        this.challengeRepository = challengeRepository;
        this.checkInRepository = checkInRepository;
        this.participantJpaRepository = participantJpaRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FriendChallengeCheckIn registerCheckIn(UUID userId, UUID challengeId,
                                                   double distanceKm, int durationSeconds,
                                                   LocalDate checkInDate, String notes) {
        FriendChallenge challenge = loadVisibleChallenge(challengeId);

        if (!participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)) {
            throw new FriendChallengeAccessDeniedException();
        }

        if (FriendChallenge.STATUS_AUDIT.equals(challenge.status())
                || !checkInDate.isBefore(challenge.endDate())) {
            throw new CheckInAuditDateException();
        }
        if (!FriendChallenge.STATUS_ACTIVE.equals(challenge.status())) {
            throw new ChallengeNotActiveException();
        }

        if (challenge.challengeType() == ChallengeType.CHECK_IN) {
            if (checkInRepository.existsValidByChallengeIdAndUserIdAndDate(challengeId, userId, checkInDate)) {
                throw new DuplicateCheckInException();
            }
        }

        long paceSecondsPerKm = Math.round(durationSeconds / distanceKm);

        FriendChallengeCheckIn checkIn = new FriendChallengeCheckIn(
                null, challengeId, userId,
                distanceKm, durationSeconds, paceSecondsPerKm,
                checkInDate, notes, FriendChallengeCheckIn.STATUS_VALID, null
        );

        return checkInRepository.save(checkIn);
    }

    @Transactional
    public List<CheckInWithUserName> listCheckIns(UUID userId, UUID challengeId) {
        FriendChallenge challenge = loadVisibleChallenge(challengeId);

        if (!participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)) {
            throw new FriendChallengeAccessDeniedException();
        }

        List<FriendChallengeCheckIn> checkIns = checkInRepository.findAllByChallengeId(challengeId);

        return checkIns.stream()
                .map(ci -> {
                    String name = userRepository.findById(ci.userId())
                            .map(u -> u.getName())
                            .orElse("Desconhecido");
                    return new CheckInWithUserName(ci, name);
                })
                .toList();
    }

    @Transactional
    public RankingView getRanking(UUID userId, UUID challengeId) {
        FriendChallenge challenge = loadVisibleChallenge(challengeId);

        if (!participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)) {
            throw new FriendChallengeAccessDeniedException();
        }

        List<FriendChallengeCheckIn> checkIns = checkInRepository.findAllByChallengeId(challengeId).stream()
                .filter(checkIn -> FriendChallengeCheckIn.STATUS_VALID.equals(checkIn.status()))
                .toList();

        List<RankingEntry> entries = buildRanking(challenge, checkIns);

        return new RankingView(challenge.challengeType(), entries);
    }

    @Transactional
    public FriendChallengeCheckIn rejectCheckIn(UUID userId, UUID challengeId, UUID checkInId) {
        FriendChallenge challenge = loadVisibleChallenge(challengeId);

        if (!challenge.creatorId().equals(userId)) {
            throw new FriendChallengeAccessDeniedException();
        }
        if (!FriendChallenge.STATUS_AUDIT.equals(challenge.status())) {
            throw new ChallengeNotInAuditException();
        }

        FriendChallengeCheckIn checkIn = checkInRepository.findById(checkInId)
                .filter(found -> found.challengeId().equals(challengeId))
                .orElseThrow(CheckInNotFoundException::new);
        if (FriendChallengeCheckIn.STATUS_REJECTED.equals(checkIn.status())) {
            throw new CheckInAlreadyRejectedException();
        }

        return checkInRepository.updateStatus(checkInId, FriendChallengeCheckIn.STATUS_REJECTED);
    }

    private List<RankingEntry> buildRanking(FriendChallenge challenge,
                                             List<FriendChallengeCheckIn> checkIns) {
        Map<UUID, List<FriendChallengeCheckIn>> byUser = checkIns.stream()
                .collect(Collectors.groupingBy(FriendChallengeCheckIn::userId));

        record UserScore(UUID userId, double score, int checkInCount) {}

        List<UserScore> scores = byUser.entrySet().stream()
                .map(entry -> {
                    UUID uid = entry.getKey();
                    List<FriendChallengeCheckIn> userCheckIns = entry.getValue();
                    double score = computeScore(challenge, userCheckIns);
                    int checkInCount = userCheckIns.size();
                    return new UserScore(uid, score, checkInCount);
                })
                .toList();

        Comparator<UserScore> comparator = switch (challenge.challengeType()) {
            case PACE -> Comparator.comparingDouble(UserScore::score); // lower is better
            default -> Comparator.comparingDouble(UserScore::score).reversed(); // higher is better
        };

        List<UserScore> sorted = scores.stream().sorted(comparator).toList();

        List<RankingEntry> entries = new ArrayList<>();
        int position = 1;
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0 && sorted.get(i).score() != sorted.get(i - 1).score()) {
                position = i + 1;
            }
            UserScore us = sorted.get(i);
            String name = userRepository.findById(us.userId())
                    .map(u -> u.getName())
                    .orElse("Desconhecido");
            entries.add(new RankingEntry(position, us.userId(), name, us.score(), us.checkInCount()));
        }

        return entries;
    }

    private double computeScore(FriendChallenge challenge, List<FriendChallengeCheckIn> userCheckIns) {
        return switch (challenge.challengeType()) {
            case DISTANCE -> userCheckIns.stream()
                    .mapToDouble(FriendChallengeCheckIn::distanceKm)
                    .sum();
            case ACTIVITY_TIME -> userCheckIns.stream()
                    .mapToDouble(FriendChallengeCheckIn::durationSeconds)
                    .sum();
            case PACE -> {
                BigDecimal goalValue = challenge.goalValue();
                List<FriendChallengeCheckIn> eligible = goalValue != null
                        ? userCheckIns.stream()
                            .filter(ci -> ci.distanceKm() >= goalValue.doubleValue())
                            .toList()
                        : userCheckIns;
                yield eligible.stream()
                        .mapToLong(FriendChallengeCheckIn::paceSecondsPerKm)
                        .min()
                        .stream()
                        .mapToDouble(v -> (double) v)
                        .findFirst()
                        .orElse(Double.MAX_VALUE);
            }
            case CHECK_IN -> userCheckIns.stream()
                    .map(FriendChallengeCheckIn::checkInDate)
                    .distinct()
                    .count();
        };
    }

    private FriendChallenge loadVisibleChallenge(UUID challengeId) {
        FriendChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new FriendChallengeNotFoundException(challengeId));
        if (FriendChallenge.STATUS_DELETED.equals(challenge.status())) {
            throw new FriendChallengeNotFoundException(challengeId);
        }
        return FriendChallengeLifecycle.applyTransitionIfNeeded(
                challenge, challengeRepository, LocalDate.now());
    }
}
