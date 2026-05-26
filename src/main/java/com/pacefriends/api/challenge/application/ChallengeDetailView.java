package com.pacefriends.api.challenge.application;

import com.pacefriends.api.challenge.domain.Activity;
import com.pacefriends.api.challenge.domain.ChallengeProgress;

import java.util.List;

public record ChallengeDetailView(ChallengeProgress progress, List<Activity> activities) {}
