package com.pacefriends.api.streak.application;

import com.pacefriends.api.profile.domain.UserObjective;
import com.pacefriends.api.profile.domain.UserSettings;
import com.pacefriends.api.profile.domain.WeeklyFrequency;
import com.pacefriends.api.profile.infrastructure.UserSettingsRepository;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateWeeklyFrequencyServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSettingsRepository userSettingsRepository;

    @ParameterizedTest
    @CsvSource({
            "2026-05-24, 2026-05-31",
            "2026-05-26, 2026-05-31",
            "2026-05-30, 2026-05-31"
    })
    void nextSunday_neverChangesTheWeekAlreadyInProgress(LocalDate changedOn,
                                                          LocalDate effectiveFrom) {
        UpdateWeeklyFrequencyService service =
                new UpdateWeeklyFrequencyService(userRepository, userSettingsRepository);

        assertThat(service.nextSunday(changedOn)).isEqualTo(effectiveFrom);
    }

    @Test
    void update_schedulesNewFrequencyForNextSunday() {
        UUID userId = UUID.randomUUID();
        UpdateWeeklyFrequencyService service =
                new UpdateWeeklyFrequencyService(userRepository, userSettingsRepository);
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder()
                .googleId("google-id").email("runner@example.com").name("Runner").build()));
        when(userSettingsRepository.findActive(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.of(UserSettings.builder()
                        .userId(userId)
                        .objective(UserObjective.IMPROVE_FITNESS)
                        .weeklyFrequency(WeeklyFrequency.THREE)
                        .effectiveFrom(LocalDate.now())
                        .build()));

        UpdateWeeklyFrequencyService.FrequencyUpdate result = service.update(userId, 5);

        assertThat(result.weeklyFrequency()).isEqualTo(5);
        assertThat(result.effectiveFrom().getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
        verify(userSettingsRepository).save(eq(userId), eq(UserObjective.IMPROVE_FITNESS),
                eq(WeeklyFrequency.FIVE), eq(result.effectiveFrom()));
    }
}
