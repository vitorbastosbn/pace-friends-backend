package com.pacefriends.api.profile.application;

import com.pacefriends.api.profile.domain.ProfileData;
import com.pacefriends.api.profile.domain.ProfileStats;
import com.pacefriends.api.profile.domain.UserObjective;
import com.pacefriends.api.profile.domain.UserSettings;
import com.pacefriends.api.profile.domain.WeeklyFrequency;
import com.pacefriends.api.profile.domain.exception.ProfileAccessDeniedException;
import com.pacefriends.api.profile.domain.exception.UserNotFoundException;
import com.pacefriends.api.profile.infrastructure.UserSettingsRepository;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;

    public ProfileService(UserRepository userRepository, UserSettingsRepository userSettingsRepository) {
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
    }

    @Transactional
    public ProfileData getUserProfile(UUID requestingUserId, UUID targetUserId) {
        validateAccess(requestingUserId, targetUserId);

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException(targetUserId));

        UserSettings settings = userSettingsRepository
                .findActive(targetUserId, LocalDate.now())
                .orElseGet(() -> userSettingsRepository.save(
                        targetUserId,
                        UserObjective.IMPROVE_FITNESS,
                        WeeklyFrequency.THREE,
                        LocalDate.now()));

        return ProfileData.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .photoUrl(user.getPhotoUrl())
                .objective(settings.getObjective())
                .weeklyFrequency(settings.getWeeklyFrequency())
                .effectiveFrom(settings.getEffectiveFrom())
                .stats(ProfileStats.empty())
                .build();
    }

    @Transactional
    public ProfileData updateProfile(UUID requestingUserId, UUID targetUserId,
                                     UserObjective newObjective, WeeklyFrequency newWeeklyFrequency) {
        validateAccess(requestingUserId, targetUserId);

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException(targetUserId));

        UserSettings current = userSettingsRepository
                .findActive(targetUserId, LocalDate.now())
                .orElse(null);

        LocalDate effectiveFrom = getNextMondayEffectiveFrom(LocalDate.now());

        auditChanges(targetUserId, current, newObjective, newWeeklyFrequency);

        UserSettings updated = userSettingsRepository.save(
                targetUserId, newObjective, newWeeklyFrequency, effectiveFrom);

        log.debug("Profile updated for userId={}, effectiveFrom={}", targetUserId, effectiveFrom);

        return ProfileData.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .photoUrl(user.getPhotoUrl())
                .objective(updated.getObjective())
                .weeklyFrequency(updated.getWeeklyFrequency())
                .effectiveFrom(updated.getEffectiveFrom())
                .stats(ProfileStats.empty())
                .build();
    }

    LocalDate getNextMondayEffectiveFrom(LocalDate from) {
        if (from.getDayOfWeek() == DayOfWeek.MONDAY) {
            return from.plusWeeks(1);
        }
        return from.with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    LocalDate getNextSundayEffectiveFrom(LocalDate from) {
        if (from.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return from.plusWeeks(1);
        }
        return from.with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.SUNDAY));
    }

    private void validateAccess(UUID requestingUserId, UUID targetUserId) {
        if (!requestingUserId.equals(targetUserId)) {
            throw new ProfileAccessDeniedException();
        }
    }

    private void auditChanges(UUID userId, UserSettings current,
                              UserObjective newObjective, WeeklyFrequency newFrequency) {
        if (current == null) {
            userSettingsRepository.saveAudit(userId, "objective", null, newObjective.name(), userId);
            userSettingsRepository.saveAudit(userId, "weekly_frequency", null, newFrequency.name(), userId);
            return;
        }
        if (!current.getObjective().equals(newObjective)) {
            userSettingsRepository.saveAudit(
                    userId, "objective",
                    current.getObjective().name(),
                    newObjective.name(),
                    userId);
        }
        if (!current.getWeeklyFrequency().equals(newFrequency)) {
            userSettingsRepository.saveAudit(
                    userId, "weekly_frequency",
                    current.getWeeklyFrequency().name(),
                    newFrequency.name(),
                    userId);
        }
    }
}
