package com.pacefriends.api.home.application;

import com.pacefriends.api.challenge.domain.ActivityRepository;
import com.pacefriends.api.home.domain.HomeSummary;
import com.pacefriends.api.profile.domain.UserObjective;
import com.pacefriends.api.profile.domain.UserSettings;
import com.pacefriends.api.profile.domain.WeeklyFrequency;
import com.pacefriends.api.profile.domain.exception.ProfileAccessDeniedException;
import com.pacefriends.api.profile.infrastructure.UserSettingsRepository;
import com.pacefriends.api.trail.application.TrailService;
import com.pacefriends.api.trail.domain.NextLevelRequirements;
import com.pacefriends.api.trail.domain.TrainingPath;
import com.pacefriends.api.trail.domain.TrainingPathData;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeSummaryServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSettingsRepository userSettingsRepository;
    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private TrailService trailService;

    private HomeSummaryService service;
    private UUID userId;

    @BeforeEach
    void setUp() {
        service = new HomeSummaryService(
                userRepository, userSettingsRepository, activityRepository, trailService);
        userId = UUID.randomUUID();
    }

    @Test
    void getHomeSummary_completeSummary_aggregatesProgress() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(4, 320)));
        when(userSettingsRepository.findActive(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.of(settings(WeeklyFrequency.FOUR)));
        when(activityRepository.countUniqueDaysByUserInWeek(
                eq(userId), any(LocalDate.class), any(LocalDate.class))).thenReturn(2);
        when(trailService.getTrainingPath(userId, userId)).thenReturn(path(4, 10));

        HomeSummary result = service.getHomeSummary(userId, userId);

        assertThat(result.streak().current()).isEqualTo(4);
        assertThat(result.xp().total()).isEqualTo(320);
        assertThat(result.level().current()).isEqualTo(3);
        assertThat(result.level().xpForNextLevel()).isEqualTo(600);
        assertThat(result.weeklyFrequency().daysTrained()).isEqualTo(2);
        assertThat(result.weeklyFrequency().goal()).isEqualTo(4);
        assertThat(result.trainingPath().available()).isTrue();
        assertThat(result.trainingPath().progressPercent()).isEqualTo(40);
        assertThat(result.trainingPath().currentLevel()).isEqualTo("Iniciante");
    }

    @Test
    void getHomeSummary_withoutTrainingPath_returnsUnavailableState() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(0, 0)));
        when(userSettingsRepository.findActive(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(activityRepository.countUniqueDaysByUserInWeek(
                eq(userId), any(LocalDate.class), any(LocalDate.class))).thenReturn(0);
        when(trailService.getTrainingPath(userId, userId)).thenReturn(null);

        HomeSummary result = service.getHomeSummary(userId, userId);

        assertThat(result.trainingPath().available()).isFalse();
        assertThat(result.trainingPath().currentLevel()).isNull();
        assertThat(result.trainingPath().progressPercent()).isNull();
        assertThat(result.weeklyFrequency().goal()).isEqualTo(3);
    }

    @Test
    void getHomeSummary_withoutActivitiesThisWeek_returnsZeroDaysTrained() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(1, 100)));
        when(userSettingsRepository.findActive(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.of(settings(WeeklyFrequency.THREE)));
        when(activityRepository.countUniqueDaysByUserInWeek(
                eq(userId), any(LocalDate.class), any(LocalDate.class))).thenReturn(0);
        when(trailService.getTrainingPath(userId, userId)).thenReturn(path(0, 10));

        HomeSummary result = service.getHomeSummary(userId, userId);

        assertThat(result.weeklyFrequency().daysTrained()).isZero();
    }

    @Test
    void getHomeSummary_differentUser_throwsAccessDeniedWithoutReadingData() {
        UUID requesterId = UUID.randomUUID();

        assertThatThrownBy(() -> service.getHomeSummary(requesterId, userId))
                .isInstanceOf(ProfileAccessDeniedException.class);

        verifyNoInteractions(userRepository, userSettingsRepository, activityRepository, trailService);
    }

    @Test
    void getHomeSummary_queriesActivitiesInMondayToSundayWindow() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(0, 0)));
        when(userSettingsRepository.findActive(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(trailService.getTrainingPath(userId, userId)).thenReturn(null);

        service.getHomeSummary(userId, userId);

        ArgumentCaptor<LocalDate> start = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> end = ArgumentCaptor.forClass(LocalDate.class);
        verify(activityRepository).countUniqueDaysByUserInWeek(
                eq(userId), start.capture(), end.capture());
        assertThat(start.getValue().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(end.getValue().getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
        assertThat(end.getValue()).isEqualTo(start.getValue().plusDays(6));
    }

    private User user(int streak, int xp) {
        User user = User.builder()
                .googleId("google-id")
                .email("runner@example.com")
                .name("Runner")
                .build();
        setField(user, "currentStreak", streak);
        setField(user, "totalXp", xp);
        return user;
    }

    private UserSettings settings(WeeklyFrequency frequency) {
        return UserSettings.builder()
                .userId(userId)
                .objective(UserObjective.IMPROVE_FITNESS)
                .weeklyFrequency(frequency)
                .effectiveFrom(LocalDate.now())
                .build();
    }

    private TrainingPathData path(int completedItems, int totalItems) {
        TrainingPath path = new TrainingPath(1, completedItems, totalItems, null, false, List.of());
        return new TrainingPathData(
                userId,
                1,
                "Iniciante",
                path,
                false,
                new NextLevelRequirements(false, 4, 0, 650, 0)
        );
    }

    private void setField(User user, String fieldName, int value) {
        try {
            Field field = User.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(user, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
