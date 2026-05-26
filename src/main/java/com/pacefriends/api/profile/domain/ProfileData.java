package com.pacefriends.api.profile.domain;

import java.time.LocalDate;
import java.util.UUID;

public class ProfileData {

    private final UUID userId;
    private final String name;
    private final String email;
    private final String photoUrl;
    private final UserObjective objective;
    private final WeeklyFrequency weeklyFrequency;
    private final LocalDate effectiveFrom;
    private final ProfileStats stats;

    private ProfileData(Builder builder) {
        this.userId = builder.userId;
        this.name = builder.name;
        this.email = builder.email;
        this.photoUrl = builder.photoUrl;
        this.objective = builder.objective;
        this.weeklyFrequency = builder.weeklyFrequency;
        this.effectiveFrom = builder.effectiveFrom;
        this.stats = builder.stats;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public UserObjective getObjective() {
        return objective;
    }

    public WeeklyFrequency getWeeklyFrequency() {
        return weeklyFrequency;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public ProfileStats getStats() {
        return stats;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID userId;
        private String name;
        private String email;
        private String photoUrl;
        private UserObjective objective;
        private WeeklyFrequency weeklyFrequency;
        private LocalDate effectiveFrom;
        private ProfileStats stats;

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder photoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
            return this;
        }

        public Builder objective(UserObjective objective) {
            this.objective = objective;
            return this;
        }

        public Builder weeklyFrequency(WeeklyFrequency weeklyFrequency) {
            this.weeklyFrequency = weeklyFrequency;
            return this;
        }

        public Builder effectiveFrom(LocalDate effectiveFrom) {
            this.effectiveFrom = effectiveFrom;
            return this;
        }

        public Builder stats(ProfileStats stats) {
            this.stats = stats;
            return this;
        }

        public ProfileData build() {
            return new ProfileData(this);
        }
    }
}
