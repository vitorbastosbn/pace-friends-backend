package com.pacefriends.api.challenge.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ActivityRepository {

    Activity save(Activity activity);

    List<Activity> findAllByChallengeId(UUID challengeId);

    BigDecimal sumDistanceByChallengeId(UUID challengeId);

    Set<LocalDate> findActivityDatesByUserInWeek(UUID userId, LocalDate weekStart, LocalDate weekEnd);

    int countUniqueDaysByUserInWeek(UUID userId, LocalDate weekStart, LocalDate weekEnd);

    long countByUserId(UUID userId);
}
