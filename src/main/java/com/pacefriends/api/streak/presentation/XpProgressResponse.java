package com.pacefriends.api.streak.presentation;

import com.pacefriends.api.streak.domain.XpCalculation;

public record XpProgressResponse(
        int xpPerDay,
        int potentialXp,
        int potentialXpIfBroken
) {
    public static XpProgressResponse from(XpCalculation calculation) {
        return new XpProgressResponse(
                calculation.xpPerDay(),
                calculation.potentialXp(),
                calculation.potentialXpIfBroken()
        );
    }
}
