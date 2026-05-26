package com.pacefriends.api.streak.application;

import com.pacefriends.api.challenge.domain.ActivityRepository;
import com.pacefriends.api.profile.domain.UserSettings;
import com.pacefriends.api.profile.domain.WeeklyFrequency;
import com.pacefriends.api.profile.infrastructure.UserSettingsRepository;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;
import java.util.UUID;

@Service
public class StreakQueryService {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final ActivityRepository activityRepository;

    public StreakQueryService(UserRepository userRepository,
                              UserSettingsRepository userSettingsRepository,
                              ActivityRepository activityRepository) {
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public StreakView getStreakView(UUID userId) {
        LocalDate today = LocalDate.now(ZONE);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        LocalDate sunday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate saturday = sunday.plusDays(6);

        int targetFrequency = userSettingsRepository.findActive(userId, today)
                .map(UserSettings::getWeeklyFrequency)
                .map(WeeklyFrequency::getValue)
                .orElse(WeeklyFrequency.THREE.getValue());

        Set<LocalDate> activityDates = activityRepository.findActivityDatesByUserInWeek(userId, sunday, saturday);
        int daysCompletedThisWeek = activityDates.size();
        int remainingDays = Math.max(0, targetFrequency - daysCompletedThisWeek);
        int potentialXp = targetFrequency * 10;

        return new StreakView(
                user.getCurrentStreak(),
                targetFrequency,
                daysCompletedThisWeek,
                remainingDays,
                potentialXp
        );
    }
}
