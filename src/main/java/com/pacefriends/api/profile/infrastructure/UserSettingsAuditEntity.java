package com.pacefriends.api.profile.infrastructure;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_settings_audit")
class UserSettingsAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "changed_field", nullable = false)
    private String changedField;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value", nullable = false)
    private String newValue;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by_user_id", nullable = false)
    private UUID changedByUserId;

    protected UserSettingsAuditEntity() {
    }

    UserSettingsAuditEntity(UUID userId, String changedField, String oldValue, String newValue, UUID changedByUserId) {
        this.userId = userId;
        this.changedField = changedField;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedByUserId = changedByUserId;
        this.changedAt = LocalDateTime.now();
    }

    UUID getId() {
        return id;
    }

    UUID getUserId() {
        return userId;
    }

    String getChangedField() {
        return changedField;
    }

    String getOldValue() {
        return oldValue;
    }

    String getNewValue() {
        return newValue;
    }

    LocalDateTime getChangedAt() {
        return changedAt;
    }

    UUID getChangedByUserId() {
        return changedByUserId;
    }
}
