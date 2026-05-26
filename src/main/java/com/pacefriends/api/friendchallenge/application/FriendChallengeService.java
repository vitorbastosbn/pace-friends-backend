package com.pacefriends.api.friendchallenge.application;

import com.pacefriends.api.friendchallenge.domain.ChallengeType;
import com.pacefriends.api.friendchallenge.domain.FriendChallenge;
import com.pacefriends.api.friendchallenge.domain.FriendChallengeParticipant;
import com.pacefriends.api.friendchallenge.domain.FriendChallengeRepository;
import com.pacefriends.api.friendchallenge.domain.ParticipantRole;
import com.pacefriends.api.friendchallenge.domain.exception.AlreadyParticipantException;
import com.pacefriends.api.friendchallenge.domain.exception.ChallengeFullException;
import com.pacefriends.api.friendchallenge.domain.exception.ChallengeNotActiveException;
import com.pacefriends.api.friendchallenge.domain.exception.FriendChallengeAccessDeniedException;
import com.pacefriends.api.friendchallenge.domain.exception.FriendChallengeNotFoundException;
import com.pacefriends.api.friendchallenge.domain.exception.InvalidInviteCodeException;
import com.pacefriends.api.friendchallenge.infrastructure.FriendChallengeParticipantEntity;
import com.pacefriends.api.friendchallenge.infrastructure.FriendChallengeParticipantJpaRepository;
import com.pacefriends.api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FriendChallengeService {

    private static final String INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 8;
    private static final int MAX_PARTICIPANTS = 5;
    private static final int MAX_INVITE_CODE_RETRIES = 3;

    private final FriendChallengeRepository friendChallengeRepository;
    private final FriendChallengeParticipantJpaRepository participantJpaRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public FriendChallengeService(FriendChallengeRepository friendChallengeRepository,
                                   FriendChallengeParticipantJpaRepository participantJpaRepository,
                                   UserRepository userRepository) {
        this.friendChallengeRepository = friendChallengeRepository;
        this.participantJpaRepository = participantJpaRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FriendChallenge createChallenge(UUID creatorId, String title, String description,
                                            ChallengeType challengeType, BigDecimal goalValue,
                                            LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        if (startDate.isBefore(today)) {
            throw new IllegalArgumentException("A data de inicio deve ser hoje ou futura.");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("A data de fim deve ser posterior a data de inicio.");
        }
        if (challengeType != ChallengeType.PACE) {
            if (goalValue == null || goalValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Meta obrigatoria e deve ser maior que zero para este tipo de desafio.");
            }
        }

        String inviteCode = generateUniqueInviteCode();

        FriendChallenge challenge = new FriendChallenge(
                null,
                creatorId,
                title,
                description,
                challengeType,
                goalValue,
                startDate,
                endDate,
                inviteCode,
                "ACTIVE",
                0,
                MAX_PARTICIPANTS,
                ParticipantRole.CREATOR,
                null,
                List.of()
        );

        FriendChallenge saved = friendChallengeRepository.save(challenge);

        FriendChallengeParticipantEntity creatorParticipant = new FriendChallengeParticipantEntity(
                saved.id(), creatorId, ParticipantRole.CREATOR.name()
        );
        participantJpaRepository.save(creatorParticipant);

        int count = participantJpaRepository.countByFriendChallengeId(saved.id());
        return new FriendChallenge(
                saved.id(), saved.creatorId(), saved.title(), saved.description(),
                saved.challengeType(), saved.goalValue(), saved.startDate(), saved.endDate(),
                saved.inviteCode(), saved.status(), count, MAX_PARTICIPANTS,
                ParticipantRole.CREATOR, saved.createdAt(), List.of()
        );
    }

    @Transactional
    public FriendChallenge joinChallenge(UUID userId, String inviteCode) {
        FriendChallenge challenge = applyTransitionIfNeeded(
                friendChallengeRepository.findByInviteCode(inviteCode)
                        .orElseThrow(InvalidInviteCodeException::new)
        );

        if (!FriendChallenge.STATUS_ACTIVE.equals(challenge.status())) {
            throw new ChallengeNotActiveException();
        }

        int count = participantJpaRepository.countByFriendChallengeId(challenge.id());
        if (count >= MAX_PARTICIPANTS) {
            throw new ChallengeFullException();
        }

        if (participantJpaRepository.existsByFriendChallengeIdAndUserId(challenge.id(), userId)) {
            throw new AlreadyParticipantException();
        }

        FriendChallengeParticipantEntity participant = new FriendChallengeParticipantEntity(
                challenge.id(), userId, ParticipantRole.MEMBER.name()
        );
        participantJpaRepository.save(participant);

        int newCount = participantJpaRepository.countByFriendChallengeId(challenge.id());
        return new FriendChallenge(
                challenge.id(), challenge.creatorId(), challenge.title(), challenge.description(),
                challenge.challengeType(), challenge.goalValue(), challenge.startDate(), challenge.endDate(),
                challenge.inviteCode(), challenge.status(), newCount, MAX_PARTICIPANTS,
                ParticipantRole.MEMBER, challenge.createdAt(), List.of()
        );
    }

    @Transactional
    public List<FriendChallenge> listChallenges(UUID userId) {
        return friendChallengeRepository.findAllByUserId(userId).stream()
                .map(this::applyTransitionIfNeeded)
                .toList();
    }

    @Transactional
    public FriendChallengeDetailView getChallengeDetail(UUID userId, UUID challengeId) {
        FriendChallenge challenge = applyTransitionIfNeeded(
                friendChallengeRepository.findById(challengeId)
                        .orElseThrow(() -> new FriendChallengeNotFoundException(challengeId))
        );

        List<FriendChallengeParticipantEntity> participantEntities =
                participantJpaRepository.findAllByFriendChallengeId(challengeId);

        FriendChallengeParticipantEntity myParticipation = participantEntities.stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(FriendChallengeAccessDeniedException::new);

        ParticipantRole myRole = ParticipantRole.valueOf(myParticipation.getRole());

        List<FriendChallengeParticipant> participants = buildParticipantList(participantEntities);

        FriendChallenge detailed = new FriendChallenge(
                challenge.id(), challenge.creatorId(), challenge.title(), challenge.description(),
                challenge.challengeType(), challenge.goalValue(), challenge.startDate(), challenge.endDate(),
                challenge.inviteCode(), challenge.status(), participantEntities.size(), MAX_PARTICIPANTS,
                myRole, challenge.createdAt(), participants
        );

        return new FriendChallengeDetailView(detailed, participants);
    }

    private FriendChallenge applyTransitionIfNeeded(FriendChallenge challenge) {
        if (!FriendChallenge.STATUS_ACTIVE.equals(challenge.status())
                && !FriendChallenge.STATUS_AUDIT.equals(challenge.status())) {
            return challenge;
        }

        String nextStatus = challenge.status();
        LocalDate today = LocalDate.now();
        if (today.isAfter(challenge.endDate())) {
            nextStatus = FriendChallenge.STATUS_FINISHED;
        } else if (today.isEqual(challenge.endDate())) {
            nextStatus = FriendChallenge.STATUS_AUDIT;
        }

        if (!nextStatus.equals(challenge.status())) {
            friendChallengeRepository.updateStatus(challenge.id(), nextStatus);
            return challenge.withStatus(nextStatus);
        }
        return challenge;
    }

    private List<FriendChallengeParticipant> buildParticipantList(
            List<FriendChallengeParticipantEntity> entities) {
        List<FriendChallengeParticipant> result = new ArrayList<>();
        for (FriendChallengeParticipantEntity p : entities) {
            String name = userRepository.findById(p.getUserId())
                    .map(u -> u.getName())
                    .orElse("Desconhecido");
            result.add(new FriendChallengeParticipant(
                    p.getUserId(),
                    name,
                    ParticipantRole.valueOf(p.getRole()),
                    p.getJoinedAt()
            ));
        }
        return result;
    }

    private String generateUniqueInviteCode() {
        for (int attempt = 0; attempt < MAX_INVITE_CODE_RETRIES; attempt++) {
            String code = generateCode();
            if (!friendChallengeRepository.existsByInviteCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Nao foi possivel gerar um codigo de convite unico. Tente novamente.");
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            sb.append(INVITE_CODE_CHARS.charAt(secureRandom.nextInt(INVITE_CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
