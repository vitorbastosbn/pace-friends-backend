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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ChallengeService challengeService;

    private UUID userId;
    private UUID challengeId;
    private Challenge activeChallenge;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        challengeId = UUID.randomUUID();
        activeChallenge = buildChallenge(challengeId, userId, ChallengeStatus.ACTIVE, new BigDecimal("50.00"));
    }

    // --- createChallenge ---

    @Test
    void createChallenge_validData_returnsCreatedChallenge() {
        Challenge saved = buildChallenge(UUID.randomUUID(), userId, ChallengeStatus.ACTIVE, new BigDecimal("50.00"));
        when(challengeRepository.save(any())).thenReturn(saved);

        Challenge result = challengeService.createChallenge(
                userId, "Run 50km", new BigDecimal("50.00"), LocalDate.now().plusDays(30));

        assertThat(result.getStatus()).isEqualTo(ChallengeStatus.ACTIVE);
        assertThat(result.getUserId()).isEqualTo(userId);
        verify(challengeRepository).save(any());
    }

    @Test
    void createChallenge_withDeadlineToday_succeeds() {
        Challenge saved = buildChallenge(UUID.randomUUID(), userId, ChallengeStatus.ACTIVE, new BigDecimal("10.00"));
        when(challengeRepository.save(any())).thenReturn(saved);

        Challenge result = challengeService.createChallenge(
                userId, "Short run", new BigDecimal("10.00"), LocalDate.now());

        assertThat(result).isNotNull();
    }

    @Test
    void createChallenge_deadlineInPast_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> challengeService.createChallenge(
                userId, "Old challenge", new BigDecimal("10.00"), LocalDate.now().minusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("prazo");

        verifyNoInteractions(challengeRepository);
    }

    @Test
    void createChallenge_savesWithActiveStatus() {
        Challenge saved = buildChallenge(UUID.randomUUID(), userId, ChallengeStatus.ACTIVE, new BigDecimal("20.00"));
        when(challengeRepository.save(any())).thenReturn(saved);

        challengeService.createChallenge(userId, "Test", new BigDecimal("20.00"), LocalDate.now().plusDays(10));

        ArgumentCaptor<Challenge> captor = ArgumentCaptor.forClass(Challenge.class);
        verify(challengeRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ChallengeStatus.ACTIVE);
    }

    // --- listChallenges ---

    @Test
    void listChallenges_returnsProgressList() {
        when(challengeRepository.findAllByUserId(userId)).thenReturn(List.of(activeChallenge));
        when(activityRepository.sumDistanceByChallengeId(challengeId)).thenReturn(new BigDecimal("10.00"));

        List<ChallengeProgress> result = challengeService.listChallenges(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProgressKm()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(result.get(0).getProgressPct()).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    void listChallenges_noActivities_returnsZeroProgress() {
        when(challengeRepository.findAllByUserId(userId)).thenReturn(List.of(activeChallenge));
        when(activityRepository.sumDistanceByChallengeId(challengeId)).thenReturn(null);

        List<ChallengeProgress> result = challengeService.listChallenges(userId);

        assertThat(result.get(0).getProgressKm()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.get(0).getProgressPct()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // --- getChallengeDetail ---

    @Test
    void getChallengeDetail_ownChallenge_returnsProgress() {
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(activeChallenge));
        when(activityRepository.sumDistanceByChallengeId(challengeId)).thenReturn(new BigDecimal("25.00"));

        ChallengeProgress result = challengeService.getChallengeDetail(userId, challengeId);

        assertThat(result.getChallenge().getId()).isEqualTo(challengeId);
        assertThat(result.getProgressPct()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void getChallengeDetail_challengeNotFound_throwsChallengeNotFoundException() {
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> challengeService.getChallengeDetail(userId, challengeId))
                .isInstanceOf(ChallengeNotFoundException.class);
    }

    @Test
    void getChallengeDetail_differentUser_throwsChallengeAccessDeniedException() {
        UUID anotherUserId = UUID.randomUUID();
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(activeChallenge));

        assertThatThrownBy(() -> challengeService.getChallengeDetail(anotherUserId, challengeId))
                .isInstanceOf(ChallengeAccessDeniedException.class);
    }

    // --- registerActivity ---

    @Test
    void registerActivity_calculatesCorrectPace() {
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(activeChallenge));
        Activity saved = buildActivity(UUID.randomUUID(), challengeId, userId,
                new BigDecimal("10.000"), 3600, new BigDecimal("360.00"));
        when(activityRepository.save(any())).thenReturn(saved);
        when(activityRepository.sumDistanceByChallengeId(challengeId)).thenReturn(new BigDecimal("10.00"));

        Activity result = challengeService.registerActivity(
                userId, challengeId, new BigDecimal("10.000"), 3600, LocalDate.now(), null);

        // pace = 3600 / 10 = 360 seconds/km = 6 min/km
        assertThat(result.getPaceSecondsPerKm()).isEqualByComparingTo(new BigDecimal("360.00"));
    }

    @Test
    void registerActivity_savesWithCorrectPaceCalculation() {
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(activeChallenge));
        when(activityRepository.save(any())).thenAnswer(inv -> {
            Activity a = inv.getArgument(0);
            return Activity.builder()
                    .id(UUID.randomUUID())
                    .challengeId(a.getChallengeId())
                    .userId(a.getUserId())
                    .distanceKm(a.getDistanceKm())
                    .durationSeconds(a.getDurationSeconds())
                    .paceSecondsPerKm(a.getPaceSecondsPerKm())
                    .activityDate(a.getActivityDate())
                    .notes(a.getNotes())
                    .createdAt(LocalDateTime.now())
                    .build();
        });
        when(activityRepository.sumDistanceByChallengeId(challengeId)).thenReturn(new BigDecimal("5.00"));

        Activity result = challengeService.registerActivity(
                userId, challengeId, new BigDecimal("5.000"), 1500, LocalDate.now(), "test");

        // pace = 1500 / 5 = 300 seconds/km = 5 min/km
        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
        verify(activityRepository).save(captor.capture());
        assertThat(captor.getValue().getPaceSecondsPerKm()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    void registerActivity_progressReachesGoal_setsStatusCompleted() {
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(activeChallenge));
        Activity saved = buildActivity(UUID.randomUUID(), challengeId, userId,
                new BigDecimal("50.000"), 18000, new BigDecimal("360.00"));
        when(activityRepository.save(any())).thenReturn(saved);
        // After saving activity, total becomes 50km = goal
        when(activityRepository.sumDistanceByChallengeId(challengeId)).thenReturn(new BigDecimal("50.00"));
        when(challengeRepository.save(any())).thenReturn(
                buildChallenge(challengeId, userId, ChallengeStatus.COMPLETED, new BigDecimal("50.00")));

        challengeService.registerActivity(userId, challengeId, new BigDecimal("50.000"), 18000, LocalDate.now(), null);

        ArgumentCaptor<Challenge> captor = ArgumentCaptor.forClass(Challenge.class);
        verify(challengeRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ChallengeStatus.COMPLETED);
    }

    @Test
    void registerActivity_progressBelowGoal_doesNotComplete() {
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(activeChallenge));
        Activity saved = buildActivity(UUID.randomUUID(), challengeId, userId,
                new BigDecimal("10.000"), 3600, new BigDecimal("360.00"));
        when(activityRepository.save(any())).thenReturn(saved);
        when(activityRepository.sumDistanceByChallengeId(challengeId)).thenReturn(new BigDecimal("10.00"));

        challengeService.registerActivity(userId, challengeId, new BigDecimal("10.000"), 3600, LocalDate.now(), null);

        // Challenge save should NOT be called since progress (10) < goal (50)
        verify(challengeRepository, never()).save(any());
    }

    @Test
    void registerActivity_alreadyCompleted_throwsChallengeAlreadyCompletedException() {
        Challenge completedChallenge = buildChallenge(challengeId, userId, ChallengeStatus.COMPLETED, new BigDecimal("50.00"));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(completedChallenge));

        assertThatThrownBy(() -> challengeService.registerActivity(
                userId, challengeId, new BigDecimal("5.000"), 1800, LocalDate.now(), null))
                .isInstanceOf(ChallengeAlreadyCompletedException.class);
    }

    @Test
    void registerActivity_differentUser_throwsChallengeAccessDeniedException() {
        UUID anotherUserId = UUID.randomUUID();
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(activeChallenge));

        assertThatThrownBy(() -> challengeService.registerActivity(
                anotherUserId, challengeId, new BigDecimal("5.000"), 1800, LocalDate.now(), null))
                .isInstanceOf(ChallengeAccessDeniedException.class);
    }

    // --- listActivities ---

    @Test
    void listActivities_ownChallenge_returnsActivities() {
        Activity activity = buildActivity(UUID.randomUUID(), challengeId, userId,
                new BigDecimal("10.000"), 3600, new BigDecimal("360.00"));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(activeChallenge));
        when(activityRepository.findAllByChallengeId(challengeId)).thenReturn(List.of(activity));

        List<Activity> result = challengeService.listActivities(userId, challengeId);

        assertThat(result).hasSize(1);
    }

    @Test
    void listActivities_differentUser_throwsChallengeAccessDeniedException() {
        UUID anotherUserId = UUID.randomUUID();
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(activeChallenge));

        assertThatThrownBy(() -> challengeService.listActivities(anotherUserId, challengeId))
                .isInstanceOf(ChallengeAccessDeniedException.class);
    }

    // --- helpers ---

    private Challenge buildChallenge(UUID id, UUID owner, ChallengeStatus status, BigDecimal goal) {
        return Challenge.builder()
                .id(id)
                .userId(owner)
                .title("Test Challenge")
                .goalDistanceKm(goal)
                .deadline(LocalDate.now().plusDays(30))
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Activity buildActivity(UUID id, UUID challengeId, UUID userId,
                                   BigDecimal distance, int duration, BigDecimal pace) {
        return Activity.builder()
                .id(id)
                .challengeId(challengeId)
                .userId(userId)
                .distanceKm(distance)
                .durationSeconds(duration)
                .paceSecondsPerKm(pace)
                .activityDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
