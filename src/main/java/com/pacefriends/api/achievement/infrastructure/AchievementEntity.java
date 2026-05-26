package com.pacefriends.api.achievement.infrastructure;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "achievements")
public class AchievementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "criteria_type", nullable = false, length = 50)
    private String criteriaType;

    @Column(name = "criteria_value")
    private Integer criteriaValue;

    @Column(name = "icon_key", length = 50)
    private String iconKey;

    protected AchievementEntity() {
    }

    public UUID getId() { return id; }
    public String getSlug() { return slug; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCriteriaType() { return criteriaType; }
    public Integer getCriteriaValue() { return criteriaValue; }
    public String getIconKey() { return iconKey; }
}
