package com.pacefriends.api.challenge.presentation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pacefriends.api.challenge.domain.Activity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ActivityResponse(
        UUID id,
        BigDecimal distanceKm,
        Integer durationSeconds,
        BigDecimal paceSecondsPerKm,
        LocalDate activityDate,
        String notes,
        LocalDateTime createdAt
) {

    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getDistanceKm(),
                activity.getDurationSeconds(),
                activity.getPaceSecondsPerKm(),
                activity.getActivityDate(),
                activity.getNotes(),
                activity.getCreatedAt()
        );
    }
}
