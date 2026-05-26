package com.pacefriends.api.achievement.domain;

import java.util.UUID;

public record Achievement(
        UUID id,
        String slug,
        String name,
        String description,
        CriteriaType criteriaType,
        Integer criteriaValue,
        String iconKey
) {
}
