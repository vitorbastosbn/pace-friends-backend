package com.pacefriends.api.friendchallenge.presentation;

import com.pacefriends.api.friendchallenge.application.FriendChallengeDetailView;
import com.pacefriends.api.friendchallenge.domain.ChallengeType;
import com.pacefriends.api.friendchallenge.domain.FriendChallenge;
import com.pacefriends.api.friendchallenge.domain.ParticipantRole;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record FriendChallengeDetailResponse(
        UUID id,
        String title,
        String description,
        ChallengeType challengeType,
        BigDecimal goalValue,
        LocalDate startDate,
        LocalDate endDate,
        String inviteCode,
        String status,
        int participantCount,
        int maxParticipants,
        ParticipantRole myRole,
        OffsetDateTime createdAt,
        List<ParticipantResponse> participants
) {
    public static FriendChallengeDetailResponse from(FriendChallengeDetailView view) {
        FriendChallenge c = view.challenge();
        List<ParticipantResponse> participantResponses = view.participants().stream()
                .map(ParticipantResponse::from)
                .toList();
        return new FriendChallengeDetailResponse(
                c.id(),
                c.title(),
                c.description(),
                c.challengeType(),
                c.goalValue(),
                c.startDate(),
                c.endDate(),
                c.inviteCode(),
                c.status(),
                c.participantCount(),
                c.maxParticipants(),
                c.myRole(),
                c.createdAt(),
                participantResponses
        );
    }
}
