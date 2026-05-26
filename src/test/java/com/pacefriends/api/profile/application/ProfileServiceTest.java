package com.pacefriends.api.profile.application;

import com.pacefriends.api.profile.domain.ProfileData;
import com.pacefriends.api.profile.domain.UserObjective;
import com.pacefriends.api.profile.domain.UserSettings;
import com.pacefriends.api.profile.domain.WeeklyFrequency;
import com.pacefriends.api.profile.domain.exception.ProfileAccessDeniedException;
import com.pacefriends.api.profile.domain.exception.UserNotFoundException;
import com.pacefriends.api.profile.infrastructure.UserSettingsRepository;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @InjectMocks
    private ProfileService profileService;

    private UUID userId;
    private User user;
    private UserSettings userSettings;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = buildUser(userId);
        userSettings = buildSettings(userId, UserObjective.LOSE_WEIGHT, WeeklyFrequency.THREE,
                LocalDate.now().minusDays(7));
    }

    // --- getNextMondayEffectiveFrom ---

    @Test
    void getNextMondayEffectiveFrom_whenMonday_returnsFollowingMonday() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        if (monday.isBefore(LocalDate.now())) {
            monday = monday.plusWeeks(1);
        }
        // ensure it's a Monday
        LocalDate aMonday = LocalDate.of(2026, 5, 25); // known Monday

        LocalDate result = profileService.getNextMondayEffectiveFrom(aMonday);

        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(result).isEqualTo(aMonday.plusWeeks(1));
    }

    @Test
    void getNextMondayEffectiveFrom_whenNotMonday_returnsNextMonday() {
        LocalDate wednesday = LocalDate.of(2026, 5, 27); // Wednesday

        LocalDate result = profileService.getNextMondayEffectiveFrom(wednesday);

        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(result).isEqualTo(LocalDate.of(2026, 6, 1));
    }

    @Test
    void getNextMondayEffectiveFrom_whenSunday_returnsNextDay() {
        LocalDate sunday = LocalDate.of(2026, 5, 31); // Sunday

        LocalDate result = profileService.getNextMondayEffectiveFrom(sunday);

        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(result).isEqualTo(LocalDate.of(2026, 6, 1));
    }

    // --- getUserProfile ---

    @Test
    void getUserProfile_validRequest_returnsProfileData() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userSettingsRepository.findActive(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.of(userSettings));

        ProfileData result = profileService.getUserProfile(userId, userId);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getObjective()).isEqualTo(UserObjective.LOSE_WEIGHT);
        assertThat(result.getWeeklyFrequency()).isEqualTo(WeeklyFrequency.THREE);
        assertThat(result.getStats().getTotalXp()).isZero();
    }

    @Test
    void getUserProfile_differentUser_throwsProfileAccessDeniedException() {
        UUID anotherUserId = UUID.randomUUID();

        assertThatThrownBy(() -> profileService.getUserProfile(anotherUserId, userId))
                .isInstanceOf(ProfileAccessDeniedException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserProfile_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getUserProfile(userId, userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getUserProfile_noActiveSettings_createsDefaultsAndReturns() {
        UserSettings defaults = buildSettings(userId, UserObjective.IMPROVE_FITNESS, WeeklyFrequency.THREE, LocalDate.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userSettingsRepository.findActive(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(userSettingsRepository.save(eq(userId), eq(UserObjective.IMPROVE_FITNESS),
                eq(WeeklyFrequency.THREE), any(LocalDate.class)))
                .thenReturn(defaults);

        ProfileData result = profileService.getUserProfile(userId, userId);

        assertThat(result.getObjective()).isEqualTo(UserObjective.IMPROVE_FITNESS);
        assertThat(result.getWeeklyFrequency()).isEqualTo(WeeklyFrequency.THREE);
        verify(userSettingsRepository).save(eq(userId), eq(UserObjective.IMPROVE_FITNESS),
                eq(WeeklyFrequency.THREE), any(LocalDate.class));
    }

    // --- updateProfile ---

    @Test
    void updateProfile_validRequest_savesAndReturnsUpdatedData() {
        UserSettings savedSettings = buildSettings(userId, UserObjective.GAIN_MUSCLE, WeeklyFrequency.FIVE,
                LocalDate.now().plusDays(6));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userSettingsRepository.findActive(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.of(userSettings));
        when(userSettingsRepository.save(eq(userId), eq(UserObjective.GAIN_MUSCLE),
                eq(WeeklyFrequency.FIVE), any(LocalDate.class)))
                .thenReturn(savedSettings);

        ProfileData result = profileService.updateProfile(userId, userId,
                UserObjective.GAIN_MUSCLE, WeeklyFrequency.FIVE);

        assertThat(result.getObjective()).isEqualTo(UserObjective.GAIN_MUSCLE);
        assertThat(result.getWeeklyFrequency()).isEqualTo(WeeklyFrequency.FIVE);
        verify(userSettingsRepository, atLeastOnce()).saveAudit(any(), any(), any(), any(), any());
    }

    @Test
    void updateProfile_noExistingSettings_auditsInitialValues() {
        UserSettings savedSettings = buildSettings(userId, UserObjective.GAIN_MUSCLE, WeeklyFrequency.FIVE,
                LocalDate.now().plusDays(6));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userSettingsRepository.findActive(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(userSettingsRepository.save(eq(userId), eq(UserObjective.GAIN_MUSCLE),
                eq(WeeklyFrequency.FIVE), any(LocalDate.class)))
                .thenReturn(savedSettings);

        profileService.updateProfile(userId, userId, UserObjective.GAIN_MUSCLE, WeeklyFrequency.FIVE);

        verify(userSettingsRepository).saveAudit(eq(userId), eq("objective"), isNull(), eq("GAIN_MUSCLE"), eq(userId));
        verify(userSettingsRepository).saveAudit(eq(userId), eq("weekly_frequency"), isNull(), eq("FIVE"), eq(userId));
    }

    @Test
    void updateProfile_differentUser_throwsProfileAccessDeniedException() {
        UUID anotherUserId = UUID.randomUUID();

        assertThatThrownBy(() -> profileService.updateProfile(
                anotherUserId, userId, UserObjective.MAINTAIN, WeeklyFrequency.THREE))
                .isInstanceOf(ProfileAccessDeniedException.class);

        verifyNoInteractions(userRepository);
        verifyNoInteractions(userSettingsRepository);
    }

    @Test
    void updateProfile_effectiveFromIsAlwaysNextMonday() {
        UserSettings savedSettings = buildSettings(userId, UserObjective.MAINTAIN, WeeklyFrequency.FOUR,
                LocalDate.now().plusDays(6));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userSettingsRepository.findActive(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.of(userSettings));
        when(userSettingsRepository.save(eq(userId), any(), any(), any()))
                .thenReturn(savedSettings);

        profileService.updateProfile(userId, userId, UserObjective.MAINTAIN, WeeklyFrequency.FOUR);

        verify(userSettingsRepository).save(eq(userId), eq(UserObjective.MAINTAIN),
                eq(WeeklyFrequency.FOUR),
                argThat(date -> date.getDayOfWeek() == DayOfWeek.MONDAY && !date.isBefore(LocalDate.now())));
    }

    // --- helpers ---

    private User buildUser(UUID id) {
        User u = User.builder()
                .googleId("google-id")
                .email("user@example.com")
                .name("Test User")
                .photoUrl("https://photo.url")
                .build();
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(u, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return u;
    }

    private UserSettings buildSettings(UUID userId, UserObjective objective,
                                       WeeklyFrequency frequency, LocalDate effectiveFrom) {
        return UserSettings.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .objective(objective)
                .weeklyFrequency(frequency)
                .effectiveFrom(effectiveFrom)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
