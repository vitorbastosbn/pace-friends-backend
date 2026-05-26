package com.pacefriends.api.profile.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface UserSettingsAuditJpaRepository extends JpaRepository<UserSettingsAuditEntity, UUID> {
}
