package com.pacefriends.api.streak.application;

import com.pacefriends.api.streak.domain.StreakResult;

public final class StreakCalculator {

    private StreakCalculator() {
    }

    public static StreakResult calculate(int daysCompleted, int targetFrequency) {
        return daysCompleted >= targetFrequency ? StreakResult.MAINTAINED : StreakResult.BROKEN;
    }

    public static int xpDelta(int targetFrequency, StreakResult result) {
        int base = targetFrequency * 10;
        return result == StreakResult.MAINTAINED ? base : -base;
    }
}
