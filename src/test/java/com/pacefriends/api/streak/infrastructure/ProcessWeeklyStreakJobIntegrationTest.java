package com.pacefriends.api.streak.infrastructure;

import com.pacefriends.api.challenge.domain.Activity;
import com.pacefriends.api.challenge.domain.ActivityRepository;
import com.pacefriends.api.challenge.domain.Challenge;
import com.pacefriends.api.challenge.domain.ChallengeRepository;
import com.pacefriends.api.challenge.domain.ChallengeStatus;
import com.pacefriends.api.profile.domain.UserObjective;
import com.pacefriends.api.profile.domain.WeeklyFrequency;
import com.pacefriends.api.profile.infrastructure.UserSettingsRepository;
import com.pacefriends.api.streak.application.ProcessWeeklyStreakService;
import com.pacefriends.api.streak.domain.StreakResult;
import com.pacefriends.api.streak.domain.WeeklyStreak;
import com.pacefriends.api.streak.domain.WeeklyStreakRepository;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProcessWeeklyStreakJobIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserSettingsRepository userSettingsRepository;
    @Autowired
    private ChallengeRepository challengeRepository;
    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private WeeklyStreakRepository weeklyStreakRepository;
    @Autowired
    private ProcessWeeklyStreakService processWeeklyStreakService;

    @Test
    void run_countsUniqueActivityDaysAndPersistsMaintainedStreak() {
        User user = userRepository.save(User.builder()
                .googleId("job-runner")
                .email("job-runner@example.com")
                .name("Job Runner")
                .build());
        userSettingsRepository.save(user.getId(), UserObjective.IMPROVE_FITNESS,
                WeeklyFrequency.FOUR, LocalDate.of(2026, 5, 24));
        Challenge challenge = challengeRepository.save(Challenge.builder()
                .userId(user.getId())
                .title("Weekly streak validation")
                .goalDistanceKm(new BigDecimal("100.00"))
                .deadline(LocalDate.of(2026, 6, 30))
                .status(ChallengeStatus.ACTIVE)
                .build());

        saveActivity(challenge, user, LocalDate.of(2026, 5, 24));
        saveActivity(challenge, user, LocalDate.of(2026, 5, 25));
        saveActivity(challenge, user, LocalDate.of(2026, 5, 25));
        saveActivity(challenge, user, LocalDate.of(2026, 5, 27));
        saveActivity(challenge, user, LocalDate.of(2026, 5, 29));

        Clock sundayMidnight = Clock.fixed(
                Instant.parse("2026-05-31T03:00:00Z"),
                ZoneId.of("America/Sao_Paulo"));
        new ProcessWeeklyStreakJob(userRepository, processWeeklyStreakService, sundayMidnight).run();

        WeeklyStreak streak = weeklyStreakRepository
                .findByUserIdAndWeekStart(user.getId(), LocalDate.of(2026, 5, 24))
                .orElseThrow();
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();

        assertThat(streak.getDaysCompleted()).isEqualTo(4);
        assertThat(streak.getResult()).isEqualTo(StreakResult.MAINTAINED);
        assertThat(streak.getXpEarned()).isEqualTo(40);
        assertThat(updatedUser.getCurrentStreak()).isEqualTo(1);
        assertThat(updatedUser.getTotalXp()).isEqualTo(40);
    }

    private void saveActivity(Challenge challenge, User user, LocalDate activityDate) {
        activityRepository.save(Activity.builder()
                .challengeId(challenge.getId())
                .userId(user.getId())
                .distanceKm(new BigDecimal("5.000"))
                .durationSeconds(1800)
                .paceSecondsPerKm(new BigDecimal("360.00"))
                .activityDate(activityDate)
                .build());
    }
}
