package com.pacefriends.api.profile.infrastructure;

import com.pacefriends.api.profile.domain.UserObjective;
import com.pacefriends.api.profile.domain.UserSettings;
import com.pacefriends.api.profile.domain.WeeklyFrequency;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserSettingsRepository {

    private final UserSettingsJpaRepository jpaRepository;
    private final UserSettingsAuditJpaRepository auditJpaRepository;

    public UserSettingsRepository(
            UserSettingsJpaRepository jpaRepository,
            UserSettingsAuditJpaRepository auditJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.auditJpaRepository = auditJpaRepository;
    }

    public Optional<UserSettings> findActive(UUID userId, LocalDate today) {
        return jpaRepository.findActiveByUserId(userId, today)
                .map(UserSettingsMapper::toDomain);
    }

    public UserSettings save(UUID userId, UserObjective objective, WeeklyFrequency weeklyFrequency, LocalDate effectiveFrom) {
        UserSettingsEntity entity = new UserSettingsEntity(
                userId,
                objective.name(),
                weeklyFrequency.name(),
                effectiveFrom
        );
        return UserSettingsMapper.toDomain(jpaRepository.save(entity));
    }

    public void saveAudit(UUID userId, String changedField, String oldValue, String newValue, UUID changedByUserId) {
        UserSettingsAuditEntity audit = new UserSettingsAuditEntity(
                userId, changedField, oldValue, newValue, changedByUserId);
        auditJpaRepository.save(audit);
    }
}
