package com.pacefriends.api.streak.infrastructure;

import com.pacefriends.api.streak.application.ProcessWeeklyStreakService;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessWeeklyStreakJobTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProcessWeeklyStreakService processWeeklyStreakService;

    @Test
    void run_usesTheScheduledSundayDateWhenProcessingEveryUser() throws Exception {
        User user = User.builder()
                .googleId("google-id")
                .email("runner@example.com")
                .name("Runner")
                .build();
        UUID userId = UUID.randomUUID();
        Field field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, userId);
        when(userRepository.findAll()).thenReturn(List.of(user));
        Clock sundayMidnight = Clock.fixed(
                Instant.parse("2026-05-31T03:00:00Z"),
                ZoneId.of("America/Sao_Paulo"));

        new ProcessWeeklyStreakJob(userRepository, processWeeklyStreakService, sundayMidnight).run();

        verify(processWeeklyStreakService).process(eq(userId), eq(LocalDate.of(2026, 5, 31)));
    }

    @Test
    void run_isScheduledForSundayAtMidnightInSaoPaulo() throws Exception {
        Scheduled schedule = ProcessWeeklyStreakJob.class.getMethod("run").getAnnotation(Scheduled.class);

        assertThat(schedule).isNotNull();
        assertThat(schedule.cron()).isEqualTo("0 0 0 * * SUN");
        assertThat(schedule.zone()).isEqualTo("America/Sao_Paulo");
    }
}
