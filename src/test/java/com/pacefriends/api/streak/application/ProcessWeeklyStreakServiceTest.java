package com.pacefriends.api.streak.application;

import com.pacefriends.api.challenge.domain.ActivityRepository;
import com.pacefriends.api.profile.domain.UserObjective;
import com.pacefriends.api.profile.domain.UserSettings;
import com.pacefriends.api.profile.domain.WeeklyFrequency;
import com.pacefriends.api.profile.infrastructure.UserSettingsRepository;
import com.pacefriends.api.streak.domain.StreakResult;
import com.pacefriends.api.streak.domain.WeeklyStreak;
import com.pacefriends.api.streak.domain.WeeklyStreakRepository;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessWeeklyStreakServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSettingsRepository userSettingsRepository;
    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private WeeklyStreakRepository weeklyStreakRepository;

    private ProcessWeeklyStreakService service;
    private UUID userId;
    private User user;
    private LocalDate processedSunday;

    @BeforeEach
    void setUp() {
        service = new ProcessWeeklyStreakService(
                userRepository, userSettingsRepository, activityRepository,
                weeklyStreakRepository, new WeeklyStreakProcessor());
        userId = UUID.randomUUID();
        user = User.builder()
                .googleId("google-id")
                .email("runner@example.com")
                .name("Runner")
                .build();
        processedSunday = LocalDate.of(2026, 5, 31);
    }

    @Test
    void process_whenTargetMet_persistsMaintainedResultAndAwardsXp() {
        when(weeklyStreakRepository.findByUserIdAndWeekStart(userId, LocalDate.of(2026, 5, 24)))
                .thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userSettingsRepository.findActive(userId, LocalDate.of(2026, 5, 30)))
                .thenReturn(Optional.of(settings(WeeklyFrequency.FOUR)));
        when(activityRepository.countUniqueDaysByUserInWeek(
                userId, LocalDate.of(2026, 5, 24), LocalDate.of(2026, 5, 30))).thenReturn(4);

        service.process(userId, processedSunday);

        ArgumentCaptor<WeeklyStreak> saved = ArgumentCaptor.forClass(WeeklyStreak.class);
        verify(weeklyStreakRepository).save(saved.capture());
        assertThat(saved.getValue().getResult()).isEqualTo(StreakResult.MAINTAINED);
        assertThat(saved.getValue().getXpEarned()).isEqualTo(40);
        assertThat(user.getCurrentStreak()).isEqualTo(1);
        assertThat(user.getTotalXp()).isEqualTo(40);
    }

    @Test
    void process_whenResultAlreadyExists_doesNotMutateUserAgain() {
        when(weeklyStreakRepository.findByUserIdAndWeekStart(userId, LocalDate.of(2026, 5, 24)))
                .thenReturn(Optional.of(WeeklyStreak.builder().build()));

        service.process(userId, processedSunday);

        verifyNoInteractions(userRepository, userSettingsRepository, activityRepository);
        verify(weeklyStreakRepository, never()).save(any());
    }

    @Test
    void process_whenNoActivities_breaksStreakAndNeverMakesXpNegative() {
        user.incrementStreak();
        user.addXp(30);
        when(weeklyStreakRepository.findByUserIdAndWeekStart(userId, LocalDate.of(2026, 5, 24)))
                .thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userSettingsRepository.findActive(userId, LocalDate.of(2026, 5, 30)))
                .thenReturn(Optional.of(settings(WeeklyFrequency.FIVE)));
        when(activityRepository.countUniqueDaysByUserInWeek(
                userId, LocalDate.of(2026, 5, 24), LocalDate.of(2026, 5, 30))).thenReturn(0);

        service.process(userId, processedSunday);

        ArgumentCaptor<WeeklyStreak> saved = ArgumentCaptor.forClass(WeeklyStreak.class);
        verify(weeklyStreakRepository).save(saved.capture());
        assertThat(saved.getValue().getResult()).isEqualTo(StreakResult.BROKEN);
        assertThat(saved.getValue().getXpEarned()).isEqualTo(-50);
        assertThat(user.getCurrentStreak()).isZero();
        assertThat(user.getTotalXp()).isZero();
    }

    private UserSettings settings(WeeklyFrequency frequency) {
        return UserSettings.builder()
                .userId(userId)
                .objective(UserObjective.IMPROVE_FITNESS)
                .weeklyFrequency(frequency)
                .effectiveFrom(LocalDate.of(2026, 5, 24))
                .build();
    }
}
