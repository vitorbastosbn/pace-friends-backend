package com.pacefriends.api.streak.application;

import com.pacefriends.api.challenge.domain.ActivityRepository;
import com.pacefriends.api.profile.domain.UserObjective;
import com.pacefriends.api.profile.domain.UserSettings;
import com.pacefriends.api.profile.domain.WeeklyFrequency;
import com.pacefriends.api.profile.infrastructure.UserSettingsRepository;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import com.pacefriends.api.streak.domain.StreakResult;
import com.pacefriends.api.streak.domain.WeeklyStreak;
import com.pacefriends.api.streak.domain.WeeklyStreakRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreakQueryServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSettingsRepository userSettingsRepository;
    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private WeeklyStreakRepository weeklyStreakRepository;

    @Test
    void getStreakView_returnsProgressAndXpRisk() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .googleId("google-id")
                .email("runner@example.com")
                .name("Runner")
                .build();
        user.incrementStreak();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userSettingsRepository.findActive(any(), any())).thenReturn(Optional.of(
                UserSettings.builder()
                        .userId(userId)
                        .objective(UserObjective.IMPROVE_FITNESS)
                        .weeklyFrequency(WeeklyFrequency.FOUR)
                        .effectiveFrom(LocalDate.now())
                        .build()));
        when(activityRepository.countUniqueDaysByUserInWeek(any(), any(), any())).thenReturn(2);
        when(weeklyStreakRepository.findRecentByUserId(userId)).thenReturn(Optional.of(
                WeeklyStreak.builder().result(StreakResult.BROKEN).build()));

        StreakView view = new StreakQueryService(
                userRepository, userSettingsRepository, activityRepository, weeklyStreakRepository)
                .getStreakView(userId);

        assertThat(view.currentStreak()).isEqualTo(1);
        assertThat(view.remainingDays()).isEqualTo(2);
        assertThat(view.xpProgress().potentialXp()).isEqualTo(40);
        assertThat(view.xpProgress().potentialXpIfBroken()).isEqualTo(-40);
        assertThat(view.lastResult()).isEqualTo(StreakResult.BROKEN);
    }
}
