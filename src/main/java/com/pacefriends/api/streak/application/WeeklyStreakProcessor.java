package com.pacefriends.api.streak.application;

import com.pacefriends.api.streak.domain.StreakResult;
import org.springframework.stereotype.Component;

@Component
public class WeeklyStreakProcessor {

    public ProcessingResult process(int targetFrequency, int daysCompleted, int currentStreak) {
        if (targetFrequency < 1 || targetFrequency > 7) {
            throw new IllegalArgumentException("targetFrequency must be between 1 and 7");
        }
        if (daysCompleted < 0) {
            throw new IllegalArgumentException("daysCompleted cannot be negative");
        }

        StreakResult result = StreakCalculator.calculate(daysCompleted, targetFrequency);
        int streakCount = result == StreakResult.MAINTAINED ? currentStreak + 1 : 0;
        int xpEarned = StreakCalculator.xpDelta(targetFrequency, result);
        return new ProcessingResult(result, streakCount, xpEarned);
    }

    public record ProcessingResult(StreakResult result, int streakCount, int xpEarned) {
    }
}
