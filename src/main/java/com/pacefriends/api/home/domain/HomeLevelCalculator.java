package com.pacefriends.api.home.domain;

public final class HomeLevelCalculator {

    private static final int[] NEXT_LEVEL_THRESHOLDS = {100, 300, 600, 1000, 1500};

    private HomeLevelCalculator() {
    }

    public static HomeSummary.Level fromTotalXp(int totalXp) {
        int normalizedXp = Math.max(0, totalXp);

        for (int index = 0; index < NEXT_LEVEL_THRESHOLDS.length; index++) {
            int threshold = NEXT_LEVEL_THRESHOLDS[index];
            if (normalizedXp < threshold) {
                return new HomeSummary.Level(index + 1, threshold);
            }
        }

        return new HomeSummary.Level(6, null);
    }
}
