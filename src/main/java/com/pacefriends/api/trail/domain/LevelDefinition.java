package com.pacefriends.api.trail.domain;

import java.util.List;

public class LevelDefinition {

    private final int level;
    private final String name;
    private final List<TrainingPathItemDefinition> items;

    public LevelDefinition(int level, String name, List<TrainingPathItemDefinition> items) {
        this.level = level;
        this.name = name;
        this.items = List.copyOf(items);
    }

    public int getLevel() { return level; }
    public String getName() { return name; }
    public List<TrainingPathItemDefinition> getItems() { return items; }
}
