package com.pacefriends.api.streak.application;

import com.pacefriends.api.profile.domain.UserObjective;
import com.pacefriends.api.profile.domain.UserSettings;
import com.pacefriends.api.profile.domain.WeeklyFrequency;
import com.pacefriends.api.profile.infrastructure.UserSettingsRepository;
import com.pacefriends.api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Service
public class UpdateWeeklyFrequencyService {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;

    public UpdateWeeklyFrequencyService(UserRepository userRepository,
                                        UserSettingsRepository userSettingsRepository) {
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
    }

    @Transactional
    public FrequencyUpdate update(UUID userId, int frequencyValue) {
        WeeklyFrequency frequency = WeeklyFrequency.fromValue(frequencyValue);
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        LocalDate today = LocalDate.now(ZONE);
        UserSettings current = userSettingsRepository.findActive(userId, today).orElse(null);
        UserObjective objective = current == null
                ? UserObjective.IMPROVE_FITNESS
                : current.getObjective();
        LocalDate effectiveFrom = nextSunday(today);

        String oldValue = current == null ? null : current.getWeeklyFrequency().name();
        if (current == null || current.getWeeklyFrequency() != frequency) {
            userSettingsRepository.saveAudit(userId, "weekly_frequency", oldValue, frequency.name(), userId);
        }

        userSettingsRepository.save(userId, objective, frequency, effectiveFrom);
        return new FrequencyUpdate(frequency.getValue(), effectiveFrom);
    }

    LocalDate nextSunday(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return date.plusWeeks(1);
        }
        return date.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
    }

    public record FrequencyUpdate(int weeklyFrequency, LocalDate effectiveFrom) {
    }
}
