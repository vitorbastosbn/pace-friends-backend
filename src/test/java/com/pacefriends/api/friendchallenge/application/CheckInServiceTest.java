package com.pacefriends.api.friendchallenge.application;

import com.pacefriends.api.friendchallenge.domain.*;
import com.pacefriends.api.friendchallenge.domain.exception.*;
import com.pacefriends.api.friendchallenge.infrastructure.FriendChallengeParticipantJpaRepository;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckInServiceTest {

    @Mock
    private FriendChallengeRepository challengeRepository;

    @Mock
    private FriendChallengeCheckInRepository checkInRepository;

    @Mock
    private FriendChallengeParticipantJpaRepository participantJpaRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CheckInService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID challengeId = UUID.randomUUID();
    private final LocalDate today = LocalDate.now();
    private final LocalDate endDate = today.plusDays(30);

    private FriendChallenge buildChallenge(ChallengeType type, BigDecimal goalValue) {
        return buildChallenge(type, goalValue, endDate, FriendChallenge.STATUS_ACTIVE);
    }

    private FriendChallenge buildChallenge(ChallengeType type, BigDecimal goalValue,
                                           LocalDate challengeEndDate, String status) {
        return new FriendChallenge(
                challengeId, userId, "Desafio Teste", null,
                type, goalValue,
                today, challengeEndDate, "ABCD1234", status,
                2, 5, ParticipantRole.CREATOR, OffsetDateTime.now(), List.of()
        );
    }

    private FriendChallengeCheckIn buildCheckIn(UUID uid, LocalDate date, double distKm,
                                                 int durationSec, long pace) {
        return new FriendChallengeCheckIn(
                UUID.randomUUID(), challengeId, uid,
                distKm, durationSec, pace,
                date, null, "VALID", OffsetDateTime.now()
        );
    }

    // --- registerCheckIn ---

    @Test
    void registerCheckIn_happyPath_calculatesAndSaves() {
        FriendChallenge challenge = buildChallenge(ChallengeType.DISTANCE, BigDecimal.valueOf(50));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)).thenReturn(true);

        FriendChallengeCheckIn saved = buildCheckIn(userId, today, 5.0, 1800, 360);
        when(checkInRepository.save(any())).thenReturn(saved);

        FriendChallengeCheckIn result = service.registerCheckIn(userId, challengeId, 5.0, 1800, today, null);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("VALID");
        verify(checkInRepository).save(argThat(ci ->
                ci.paceSecondsPerKm() == 360L
                        && ci.status().equals("VALID")
        ));
    }

    @Test
    void registerCheckIn_challengeNotFound_throwsNotFoundException() {
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registerCheckIn(userId, challengeId, 5.0, 1800, today, null))
                .isInstanceOf(FriendChallengeNotFoundException.class);
    }

    @Test
    void registerCheckIn_notParticipant_throwsAccessDenied() {
        FriendChallenge challenge = buildChallenge(ChallengeType.DISTANCE, BigDecimal.valueOf(50));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)).thenReturn(false);

        assertThatThrownBy(() -> service.registerCheckIn(userId, challengeId, 5.0, 1800, today, null))
                .isInstanceOf(FriendChallengeAccessDeniedException.class);
    }

    @Test
    void registerCheckIn_onAuditDate_throwsCheckInAuditDateException() {
        FriendChallenge challenge = buildChallenge(ChallengeType.DISTANCE, BigDecimal.valueOf(50));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)).thenReturn(true);

        assertThatThrownBy(() -> service.registerCheckIn(userId, challengeId, 5.0, 1800, endDate, null))
                .isInstanceOf(CheckInAuditDateException.class)
                .hasMessageContaining("auditoria");
    }

    @Test
    void registerCheckIn_duringAudit_rejectsCheckInWithPreviousDate() {
        FriendChallenge challenge = buildChallenge(
                ChallengeType.DISTANCE, BigDecimal.valueOf(50), today, FriendChallenge.STATUS_ACTIVE);
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)).thenReturn(true);

        assertThatThrownBy(() -> service.registerCheckIn(
                userId, challengeId, 5.0, 1800, today.minusDays(1), null))
                .isInstanceOf(CheckInAuditDateException.class);
        verify(challengeRepository).updateStatus(challengeId, FriendChallenge.STATUS_AUDIT);
        verify(checkInRepository, never()).save(any());
    }

    @Test
    void registerCheckIn_duplicateOnCheckInType_throwsDuplicateCheckInException() {
        FriendChallenge challenge = buildChallenge(ChallengeType.CHECK_IN, BigDecimal.ONE);
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)).thenReturn(true);
        when(checkInRepository.existsValidByChallengeIdAndUserIdAndDate(challengeId, userId, today)).thenReturn(true);

        assertThatThrownBy(() -> service.registerCheckIn(userId, challengeId, 5.0, 1800, today, null))
                .isInstanceOf(DuplicateCheckInException.class)
                .hasMessageContaining("check-in neste dia");
    }

    @Test
    void registerCheckIn_distanceType_duplicateSameDayAllowed() {
        FriendChallenge challenge = buildChallenge(ChallengeType.DISTANCE, BigDecimal.valueOf(50));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)).thenReturn(true);
        FriendChallengeCheckIn saved = buildCheckIn(userId, today, 5.0, 1800, 360);
        when(checkInRepository.save(any())).thenReturn(saved);

        // No exception should be thrown for non-CHECK_IN type
        assertThatCode(() -> service.registerCheckIn(userId, challengeId, 5.0, 1800, today, null))
                .doesNotThrowAnyException();
        verify(checkInRepository, never()).existsValidByChallengeIdAndUserIdAndDate(any(), any(), any());
    }

    // --- listCheckIns ---

    @Test
    void listCheckIns_participant_returnsCheckInsWithNames() {
        FriendChallenge challenge = buildChallenge(ChallengeType.DISTANCE, BigDecimal.valueOf(50));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)).thenReturn(true);

        FriendChallengeCheckIn ci = buildCheckIn(userId, today, 5.0, 1800, 360);
        when(checkInRepository.findAllByChallengeId(challengeId)).thenReturn(List.of(ci));

        User user = mock(User.class);
        when(user.getName()).thenReturn("Joao");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        List<CheckInWithUserName> result = service.listCheckIns(userId, challengeId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).userName()).isEqualTo("Joao");
    }

    @Test
    void listCheckIns_notParticipant_throwsAccessDenied() {
        FriendChallenge challenge = buildChallenge(ChallengeType.DISTANCE, BigDecimal.valueOf(50));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)).thenReturn(false);

        assertThatThrownBy(() -> service.listCheckIns(userId, challengeId))
                .isInstanceOf(FriendChallengeAccessDeniedException.class);
    }

    @Test
    void listCheckIns_deletedChallenge_isNotAccessible() {
        FriendChallenge challenge = buildChallenge(
                ChallengeType.DISTANCE, BigDecimal.valueOf(50), endDate, FriendChallenge.STATUS_DELETED);
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> service.listCheckIns(userId, challengeId))
                .isInstanceOf(FriendChallengeNotFoundException.class);
    }

    @Test
    void registerCheckIn_deletedChallenge_isNotAccessible() {
        FriendChallenge challenge = buildChallenge(
                ChallengeType.DISTANCE, BigDecimal.valueOf(50), endDate, FriendChallenge.STATUS_DELETED);
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> service.registerCheckIn(
                userId, challengeId, 5.0, 1800, today, null))
                .isInstanceOf(FriendChallengeNotFoundException.class);
        verify(checkInRepository, never()).save(any());
    }

    // --- rejectCheckIn ---

    @Test
    void rejectCheckIn_creatorDuringAudit_marksCheckInAsRejected() {
        FriendChallenge challenge = buildChallenge(
                ChallengeType.DISTANCE, BigDecimal.valueOf(50), today, FriendChallenge.STATUS_AUDIT);
        FriendChallengeCheckIn checkIn = buildCheckIn(userId, today.minusDays(1), 5.0, 1800, 360);
        FriendChallengeCheckIn rejected = new FriendChallengeCheckIn(
                checkIn.id(), challengeId, userId, 5.0, 1800, 360,
                today.minusDays(1), null, "REJECTED", checkIn.createdAt());
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(checkInRepository.findById(checkIn.id())).thenReturn(Optional.of(checkIn));
        when(checkInRepository.updateStatus(checkIn.id(), "REJECTED")).thenReturn(rejected);

        FriendChallengeCheckIn result = service.rejectCheckIn(userId, challengeId, checkIn.id());

        assertThat(result.status()).isEqualTo("REJECTED");
        verify(checkInRepository).updateStatus(checkIn.id(), "REJECTED");
    }

    @Test
    void rejectCheckIn_nonCreator_throwsAccessDenied() {
        UUID memberId = UUID.randomUUID();
        FriendChallenge challenge = buildChallenge(
                ChallengeType.DISTANCE, BigDecimal.valueOf(50), today, FriendChallenge.STATUS_AUDIT);
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> service.rejectCheckIn(memberId, challengeId, UUID.randomUUID()))
                .isInstanceOf(FriendChallengeAccessDeniedException.class);
        verify(checkInRepository, never()).updateStatus(any(), anyString());
    }

    @Test
    void rejectCheckIn_afterAudit_throwsChallengeNotInAuditException() {
        FriendChallenge challenge = buildChallenge(
                ChallengeType.DISTANCE, BigDecimal.valueOf(50), today.minusDays(1), FriendChallenge.STATUS_AUDIT);
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> service.rejectCheckIn(userId, challengeId, UUID.randomUUID()))
                .isInstanceOf(ChallengeNotInAuditException.class);
        verify(challengeRepository).updateStatus(challengeId, FriendChallenge.STATUS_FINISHED);
    }

    @Test
    void rejectCheckIn_alreadyRejected_throwsConflictException() {
        FriendChallenge challenge = buildChallenge(
                ChallengeType.DISTANCE, BigDecimal.valueOf(50), today, FriendChallenge.STATUS_AUDIT);
        FriendChallengeCheckIn rejected = new FriendChallengeCheckIn(
                UUID.randomUUID(), challengeId, userId, 5.0, 1800, 360,
                today.minusDays(1), null, "REJECTED", OffsetDateTime.now());
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(checkInRepository.findById(rejected.id())).thenReturn(Optional.of(rejected));

        assertThatThrownBy(() -> service.rejectCheckIn(userId, challengeId, rejected.id()))
                .isInstanceOf(CheckInAlreadyRejectedException.class);
    }

    // --- getRanking DISTANCE ---

    @Test
    void getRanking_distanceType_sortsByTotalDistanceDesc() {
        UUID user2 = UUID.randomUUID();
        FriendChallenge challenge = buildChallenge(ChallengeType.DISTANCE, BigDecimal.valueOf(5));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)).thenReturn(true);

        List<FriendChallengeCheckIn> checkIns = List.of(
                buildCheckIn(userId, today, 10.0, 3600, 360),
                buildCheckIn(userId, today.plusDays(1), 5.0, 1800, 360),
                buildCheckIn(user2, today, 8.0, 2880, 360)
        );
        when(checkInRepository.findAllByChallengeId(challengeId)).thenReturn(checkIns);

        User u1 = mock(User.class); when(u1.getName()).thenReturn("Ana");
        User u2 = mock(User.class); when(u2.getName()).thenReturn("Bob");
        when(userRepository.findById(userId)).thenReturn(Optional.of(u1));
        when(userRepository.findById(user2)).thenReturn(Optional.of(u2));

        RankingView ranking = service.getRanking(userId, challengeId);

        assertThat(ranking.challengeType()).isEqualTo(ChallengeType.DISTANCE);
        assertThat(ranking.entries()).hasSize(2);
        assertThat(ranking.entries().get(0).position()).isEqualTo(1);
        assertThat(ranking.entries().get(0).score()).isEqualTo(15.0); // 10+5
        assertThat(ranking.entries().get(1).position()).isEqualTo(2);
        assertThat(ranking.entries().get(1).score()).isEqualTo(8.0);
    }

    @Test
    void getRanking_rejectedCheckIns_doNotCount() {
        FriendChallenge challenge = buildChallenge(ChallengeType.DISTANCE, BigDecimal.valueOf(5));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)).thenReturn(true);

        FriendChallengeCheckIn valid = buildCheckIn(userId, today, 5.0, 1800, 360);
        FriendChallengeCheckIn rejected = new FriendChallengeCheckIn(
                UUID.randomUUID(), challengeId, userId, 20.0, 7200, 360,
                today.plusDays(1), null, "REJECTED", OffsetDateTime.now());
        when(checkInRepository.findAllByChallengeId(challengeId)).thenReturn(List.of(valid, rejected));

        User user = mock(User.class);
        when(user.getName()).thenReturn("Ana");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        RankingView ranking = service.getRanking(userId, challengeId);

        assertThat(ranking.entries()).singleElement().satisfies(entry -> {
            assertThat(entry.score()).isEqualTo(5.0);
            assertThat(entry.checkInCount()).isEqualTo(1);
        });
    }

    @Test
    void getRanking_removedCheckIns_doNotCountAfterRejoin() {
        FriendChallenge challenge = buildChallenge(ChallengeType.DISTANCE, BigDecimal.valueOf(5));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)).thenReturn(true);

        FriendChallengeCheckIn removed = new FriendChallengeCheckIn(
                UUID.randomUUID(), challengeId, userId, 20.0, 7200, 360,
                today, null, FriendChallengeCheckIn.STATUS_REMOVED_BY_LEAVE, OffsetDateTime.now());
        when(checkInRepository.findAllByChallengeId(challengeId)).thenReturn(List.of(removed));

        RankingView ranking = service.getRanking(userId, challengeId);

        assertThat(ranking.entries()).isEmpty();
    }

    @Test
    void getRanking_deletedChallenge_isNotAccessible() {
        FriendChallenge challenge = buildChallenge(
                ChallengeType.DISTANCE, BigDecimal.valueOf(5), endDate, FriendChallenge.STATUS_DELETED);
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> service.getRanking(userId, challengeId))
                .isInstanceOf(FriendChallengeNotFoundException.class);
    }

    // --- getRanking ACTIVITY_TIME ---

    @Test
    void getRanking_activityTimeType_sortsByTotalDurationDesc() {
        UUID user2 = UUID.randomUUID();
        FriendChallenge challenge = buildChallenge(ChallengeType.ACTIVITY_TIME, BigDecimal.valueOf(60));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)).thenReturn(true);

        List<FriendChallengeCheckIn> checkIns = List.of(
                buildCheckIn(userId, today, 5.0, 1800, 360),
                buildCheckIn(user2, today, 5.0, 3600, 720)
        );
        when(checkInRepository.findAllByChallengeId(challengeId)).thenReturn(checkIns);

        User u1 = mock(User.class); when(u1.getName()).thenReturn("Ana");
        User u2 = mock(User.class); when(u2.getName()).thenReturn("Bob");
        when(userRepository.findById(userId)).thenReturn(Optional.of(u1));
        when(userRepository.findById(user2)).thenReturn(Optional.of(u2));

        RankingView ranking = service.getRanking(userId, challengeId);

        assertThat(ranking.entries().get(0).score()).isEqualTo(3600.0);
        assertThat(ranking.entries().get(1).score()).isEqualTo(1800.0);
    }

    // --- getRanking PACE ---

    @Test
    void getRanking_paceType_sortsByBestPaceAsc_onlyEligible() {
        UUID user2 = UUID.randomUUID();
        // goalValue = 5.0 km minimum
        FriendChallenge challenge = buildChallenge(ChallengeType.PACE, BigDecimal.valueOf(5.0));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)).thenReturn(true);

        List<FriendChallengeCheckIn> checkIns = List.of(
                buildCheckIn(userId, today, 5.0, 1500, 300),        // eligible, pace 300
                buildCheckIn(userId, today.plusDays(1), 3.0, 900, 300), // not eligible (3 < 5)
                buildCheckIn(user2, today, 5.0, 1800, 360)           // eligible, pace 360
        );
        when(checkInRepository.findAllByChallengeId(challengeId)).thenReturn(checkIns);

        User u1 = mock(User.class); when(u1.getName()).thenReturn("Ana");
        User u2 = mock(User.class); when(u2.getName()).thenReturn("Bob");
        when(userRepository.findById(userId)).thenReturn(Optional.of(u1));
        when(userRepository.findById(user2)).thenReturn(Optional.of(u2));

        RankingView ranking = service.getRanking(userId, challengeId);

        // lower pace = better position
        assertThat(ranking.entries().get(0).score()).isEqualTo(300.0);
        assertThat(ranking.entries().get(0).position()).isEqualTo(1);
        assertThat(ranking.entries().get(1).score()).isEqualTo(360.0);
        assertThat(ranking.entries().get(1).position()).isEqualTo(2);
    }

    // --- getRanking CHECK_IN ---

    @Test
    void getRanking_checkInType_sortsByDistinctDaysDesc() {
        UUID user2 = UUID.randomUUID();
        FriendChallenge challenge = buildChallenge(ChallengeType.CHECK_IN, BigDecimal.ONE);
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)).thenReturn(true);

        List<FriendChallengeCheckIn> checkIns = List.of(
                buildCheckIn(userId, today, 5.0, 1800, 360),
                buildCheckIn(userId, today.plusDays(1), 5.0, 1800, 360),
                buildCheckIn(userId, today.plusDays(2), 5.0, 1800, 360),
                buildCheckIn(user2, today, 5.0, 1800, 360)
        );
        when(checkInRepository.findAllByChallengeId(challengeId)).thenReturn(checkIns);

        User u1 = mock(User.class); when(u1.getName()).thenReturn("Ana");
        User u2 = mock(User.class); when(u2.getName()).thenReturn("Bob");
        when(userRepository.findById(userId)).thenReturn(Optional.of(u1));
        when(userRepository.findById(user2)).thenReturn(Optional.of(u2));

        RankingView ranking = service.getRanking(userId, challengeId);

        assertThat(ranking.entries().get(0).score()).isEqualTo(3.0);  // userId: 3 distinct days
        assertThat(ranking.entries().get(1).score()).isEqualTo(1.0);  // user2: 1 day
    }

    // --- Tie/draw ranking ---

    @Test
    void getRanking_tiedScores_samePosAndNextSkips() {
        UUID user2 = UUID.randomUUID();
        UUID user3 = UUID.randomUUID();
        FriendChallenge challenge = buildChallenge(ChallengeType.DISTANCE, BigDecimal.valueOf(10));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, userId)).thenReturn(true);

        // userId and user2 both have 10.0 km, user3 has 5.0
        List<FriendChallengeCheckIn> checkIns = List.of(
                buildCheckIn(userId, today, 10.0, 3600, 360),
                buildCheckIn(user2, today, 10.0, 3600, 360),
                buildCheckIn(user3, today, 5.0, 1800, 360)
        );
        when(checkInRepository.findAllByChallengeId(challengeId)).thenReturn(checkIns);

        User u1 = mock(User.class); when(u1.getName()).thenReturn("Ana");
        User u2 = mock(User.class); when(u2.getName()).thenReturn("Bob");
        User u3 = mock(User.class); when(u3.getName()).thenReturn("Carlos");
        when(userRepository.findById(userId)).thenReturn(Optional.of(u1));
        when(userRepository.findById(user2)).thenReturn(Optional.of(u2));
        when(userRepository.findById(user3)).thenReturn(Optional.of(u3));

        RankingView ranking = service.getRanking(userId, challengeId);

        assertThat(ranking.entries()).hasSize(3);
        List<Integer> positions = ranking.entries().stream().map(RankingEntry::position).toList();
        // first two tied at 1, third at 3
        assertThat(positions).containsExactlyInAnyOrder(1, 1, 3);
    }
}
