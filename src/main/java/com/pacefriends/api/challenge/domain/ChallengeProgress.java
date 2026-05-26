package com.pacefriends.api.challenge.domain;

import java.math.BigDecimal;

public class ChallengeProgress {

    private final Challenge challenge;
    private final BigDecimal progressKm;
    private final BigDecimal progressPct;

    public ChallengeProgress(Challenge challenge, BigDecimal progressKm, BigDecimal progressPct) {
        this.challenge = challenge;
        this.progressKm = progressKm;
        this.progressPct = progressPct;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public BigDecimal getProgressKm() {
        return progressKm;
    }

    public BigDecimal getProgressPct() {
        return progressPct;
    }
}
