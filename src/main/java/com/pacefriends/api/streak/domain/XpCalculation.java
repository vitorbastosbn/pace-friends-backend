package com.pacefriends.api.streak.domain;

public record XpCalculation(
        int xpPerDay,
        int potentialXp,
        int potentialXpIfBroken
) {
    private static final int XP_PER_DAY = 10;

    public static XpCalculation forTargetFrequency(int targetFrequency) {
        int potentialXp = targetFrequency * XP_PER_DAY;
        return new XpCalculation(XP_PER_DAY, potentialXp, -potentialXp);
    }
}
