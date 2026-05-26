package com.pacefriends.api.streak;

import com.pacefriends.api.streak.application.StreakCalculator;
import com.pacefriends.api.streak.application.WeeklyStreakProcessor;
import com.pacefriends.api.streak.domain.StreakResult;
import com.pacefriends.api.user.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklyStreakProcessorTest {

    private final WeeklyStreakProcessor processor = new WeeklyStreakProcessor();

    // --- StreakCalculator ---

    @Test
    void calculate_whenDaysCompletedMeetsTarget_returnsMaintained() {
        StreakResult result = StreakCalculator.calculate(4, 4);
        assertThat(result).isEqualTo(StreakResult.MAINTAINED);
    }

    @Test
    void calculate_whenDaysCompletedBelowTarget_returnsBroken() {
        StreakResult result = StreakCalculator.calculate(3, 5);
        assertThat(result).isEqualTo(StreakResult.BROKEN);
    }

    @Test
    void xpDelta_whenMaintained_returnsPositiveXp() {
        int delta = StreakCalculator.xpDelta(4, StreakResult.MAINTAINED);
        assertThat(delta).isEqualTo(40);
    }

    @Test
    void xpDelta_whenBroken_returnsNegativeXp() {
        int delta = StreakCalculator.xpDelta(5, StreakResult.BROKEN);
        assertThat(delta).isEqualTo(-50);
    }

    @Test
    void processor_whenTargetMet_incrementsStreakAndAwardsXp() {
        WeeklyStreakProcessor.ProcessingResult result = processor.process(4, 4, 2);

        assertThat(result.result()).isEqualTo(StreakResult.MAINTAINED);
        assertThat(result.streakCount()).isEqualTo(3);
        assertThat(result.xpEarned()).isEqualTo(40);
    }

    @Test
    void processor_whenTargetMissed_resetsStreakAndDeductsXp() {
        WeeklyStreakProcessor.ProcessingResult result = processor.process(5, 3, 4);

        assertThat(result.result()).isEqualTo(StreakResult.BROKEN);
        assertThat(result.streakCount()).isZero();
        assertThat(result.xpEarned()).isEqualTo(-50);
    }

    // --- User XP and streak mutations ---

    @Test
    void user_maintainedStreak_incrementsAndAddsXp() {
        User user = buildUser(0, 0);

        StreakResult result = StreakCalculator.calculate(4, 4);
        int delta = StreakCalculator.xpDelta(4, result);

        if (result == StreakResult.MAINTAINED) {
            user.incrementStreak();
        } else {
            user.resetStreak();
        }
        user.addXp(delta);

        assertThat(result).isEqualTo(StreakResult.MAINTAINED);
        assertThat(user.getCurrentStreak()).isEqualTo(1);
        assertThat(user.getTotalXp()).isEqualTo(40);
    }

    @Test
    void user_brokenStreak_resetsAndDeductsXp() {
        User user = buildUser(3, 100);

        StreakResult result = StreakCalculator.calculate(3, 5);
        int delta = StreakCalculator.xpDelta(5, result);

        if (result == StreakResult.MAINTAINED) {
            user.incrementStreak();
        } else {
            user.resetStreak();
        }
        user.addXp(delta);

        assertThat(result).isEqualTo(StreakResult.BROKEN);
        assertThat(user.getCurrentStreak()).isEqualTo(0);
        assertThat(user.getTotalXp()).isEqualTo(50);
    }

    @Test
    void user_xpDoesNotGoBelowZero_whenDeductionExceedsBalance() {
        User user = buildUser(2, 30);

        StreakResult result = StreakCalculator.calculate(0, 5);
        int delta = StreakCalculator.xpDelta(5, result);

        user.resetStreak();
        user.addXp(delta);

        assertThat(user.getTotalXp()).isEqualTo(0);
    }

    // --- helper ---

    private User buildUser(int currentStreak, int totalXp) {
        User u = User.builder()
                .googleId("google-id")
                .email("user@example.com")
                .name("Test User")
                .build();
        try {
            Field streakField = User.class.getDeclaredField("currentStreak");
            streakField.setAccessible(true);
            streakField.set(u, currentStreak);

            Field xpField = User.class.getDeclaredField("totalXp");
            xpField.setAccessible(true);
            xpField.set(u, totalXp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return u;
    }
}
