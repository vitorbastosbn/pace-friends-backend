package com.pacefriends.api.friendchallenge.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pacefriends.api.friendchallenge.application.CheckInWithUserName;
import com.pacefriends.api.friendchallenge.application.FriendChallengeDetailView;
import com.pacefriends.api.friendchallenge.application.RankingView;
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
        @JsonProperty("challenge_type") ChallengeType challengeType,
        @JsonProperty("goal_value") BigDecimal goalValue,
        @JsonProperty("start_date") LocalDate startDate,
        @JsonProperty("end_date") LocalDate endDate,
        @JsonProperty("invite_code") String inviteCode,
        String status,
        @JsonProperty("participant_count") int participantCount,
        @JsonProperty("max_participants") int maxParticipants,
        @JsonProperty("user_role") ParticipantRole userRole,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        List<ParticipantResponse> participants,
        List<RankingEntryResponse> ranking,
        @JsonProperty("check_ins") List<ChallengeDetailCheckInResponse> checkIns,
        ChallengePermissionsResponse permissions
) {
    public static FriendChallengeDetailResponse from(FriendChallengeDetailView view,
                                                     RankingView ranking,
                                                     List<CheckInWithUserName> checkIns) {
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
                participantResponses,
                ranking.entries().stream().map(RankingEntryResponse::from).toList(),
                checkIns.stream().map(ChallengeDetailCheckInResponse::from).toList(),
                ChallengePermissionsResponse.from(c)
        );
    }
}
