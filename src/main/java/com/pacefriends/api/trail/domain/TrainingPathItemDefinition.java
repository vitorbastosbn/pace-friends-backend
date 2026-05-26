package com.pacefriends.api.trail.domain;

public class TrainingPathItemDefinition {

    private final int position;
    private final String description;
    private final ItemCriterion criterionType;
    private final double criterionValue;
    private final int xpReward;

    public TrainingPathItemDefinition(int position, String description,
                                       ItemCriterion criterionType, double criterionValue,
                                       int xpReward) {
        this.position = position;
        this.description = description;
        this.criterionType = criterionType;
        this.criterionValue = criterionValue;
        this.xpReward = xpReward;
    }

    public int getPosition() { return position; }
    public String getDescription() { return description; }
    public ItemCriterion getCriterionType() { return criterionType; }
    public double getCriterionValue() { return criterionValue; }
    public int getXpReward() { return xpReward; }
}
