package com.pacefriends.api.home.application;

import com.pacefriends.api.challenge.domain.ActivityRepository;
import com.pacefriends.api.home.domain.HomeLevelCalculator;
import com.pacefriends.api.home.domain.HomeSummary;
import com.pacefriends.api.profile.domain.UserSettings;
import com.pacefriends.api.profile.domain.WeeklyFrequency;
import com.pacefriends.api.profile.domain.exception.ProfileAccessDeniedException;
import com.pacefriends.api.profile.domain.exception.UserNotFoundException;
import com.pacefriends.api.profile.infrastructure.UserSettingsRepository;
import com.pacefriends.api.trail.application.TrailService;
import com.pacefriends.api.trail.domain.TrainingPathData;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Service
public class HomeSummaryService {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final ActivityRepository activityRepository;
    private final TrailService trailService;

    public HomeSummaryService(UserRepository userRepository,
                              UserSettingsRepository userSettingsRepository,
                              ActivityRepository activityRepository,
                              TrailService trailService) {
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.activityRepository = activityRepository;
        this.trailService = trailService;
    }

    @Transactional(readOnly = true)
    public HomeSummary getHomeSummary(UUID requestingUserId, UUID targetUserId) {
        validateAccess(requestingUserId, targetUserId);

        LocalDate today = LocalDate.now(ZONE);
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException(targetUserId));

        int weeklyGoal = userSettingsRepository.findActive(targetUserId, today)
                .map(UserSettings::getWeeklyFrequency)
                .map(WeeklyFrequency::getValue)
                .orElse(WeeklyFrequency.THREE.getValue());

        LocalDate weekStart = startOfWeek(today);
        int daysTrained = activityRepository.countUniqueDaysByUserInWeek(
                targetUserId, weekStart, weekStart.plusDays(6));

        return new HomeSummary(
                new HomeSummary.Streak(user.getCurrentStreak(), "days"),
                new HomeSummary.Xp(user.getTotalXp()),
                HomeLevelCalculator.fromTotalXp(user.getTotalXp()),
                new HomeSummary.WeeklyFrequency(daysTrained, weeklyGoal),
                toTrainingPath(trailService.getTrainingPath(requestingUserId, targetUserId))
        );
    }

    static LocalDate startOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private HomeSummary.TrainingPath toTrainingPath(TrainingPathData pathData) {
        if (pathData == null || pathData.getPath() == null) {
            return HomeSummary.TrainingPath.unavailable();
        }

        int totalItems = pathData.getPath().getTotalItems();
        int progress = totalItems == 0
                ? 0
                : Math.round((pathData.getPath().getCompletedItems() * 100f) / totalItems);

        return new HomeSummary.TrainingPath(
                pathData.getCurrentLevelName(),
                progress,
                true
        );
    }

    private void validateAccess(UUID requestingUserId, UUID targetUserId) {
        if (!requestingUserId.equals(targetUserId)) {
            throw new ProfileAccessDeniedException();
        }
    }
}
