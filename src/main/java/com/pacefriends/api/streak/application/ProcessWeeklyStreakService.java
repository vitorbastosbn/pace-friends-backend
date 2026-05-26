package com.pacefriends.api.streak.application;

import com.pacefriends.api.challenge.domain.ActivityRepository;
import com.pacefriends.api.profile.domain.UserSettings;
import com.pacefriends.api.profile.domain.WeeklyFrequency;
import com.pacefriends.api.profile.infrastructure.UserSettingsRepository;
import com.pacefriends.api.streak.domain.StreakResult;
import com.pacefriends.api.streak.domain.WeeklyStreak;
import com.pacefriends.api.streak.domain.WeeklyStreakRepository;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ProcessWeeklyStreakService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final ActivityRepository activityRepository;
    private final WeeklyStreakRepository weeklyStreakRepository;
    private final WeeklyStreakProcessor weeklyStreakProcessor;

    public ProcessWeeklyStreakService(UserRepository userRepository,
                                      UserSettingsRepository userSettingsRepository,
                                      ActivityRepository activityRepository,
                                      WeeklyStreakRepository weeklyStreakRepository,
                                      WeeklyStreakProcessor weeklyStreakProcessor) {
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.activityRepository = activityRepository;
        this.weeklyStreakRepository = weeklyStreakRepository;
        this.weeklyStreakProcessor = weeklyStreakProcessor;
    }

    @Transactional
    public void process(UUID userId, LocalDate processedSunday) {
        LocalDate weekStart = processedSunday.minusWeeks(1);
        LocalDate weekEnd = processedSunday.minusDays(1);

        if (weeklyStreakRepository.findByUserIdAndWeekStart(userId, weekStart).isPresent()) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        int targetFrequency = userSettingsRepository.findActive(userId, weekEnd)
                .map(UserSettings::getWeeklyFrequency)
                .map(WeeklyFrequency::getValue)
                .orElse(WeeklyFrequency.THREE.getValue());

        int daysCompleted = activityRepository.countUniqueDaysByUserInWeek(userId, weekStart, weekEnd);
        WeeklyStreakProcessor.ProcessingResult processing = weeklyStreakProcessor.process(
                targetFrequency, daysCompleted, user.getCurrentStreak());

        if (processing.result() == StreakResult.MAINTAINED) {
            user.incrementStreak();
        } else {
            user.resetStreak();
        }
        user.addXp(processing.xpEarned());

        userRepository.save(user);

        WeeklyStreak streak = WeeklyStreak.builder()
                .userId(userId)
                .weekStartDate(weekStart)
                .targetFrequency(targetFrequency)
                .daysCompleted(daysCompleted)
                .streakCount(user.getCurrentStreak())
                .xpEarned(processing.xpEarned())
                .result(processing.result())
                .processedAt(LocalDateTime.now())
                .build();

        weeklyStreakRepository.save(streak);
    }
}
