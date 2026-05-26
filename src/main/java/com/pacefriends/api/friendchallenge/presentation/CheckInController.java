package com.pacefriends.api.friendchallenge.presentation;

import com.pacefriends.api.friendchallenge.application.CheckInService;
import com.pacefriends.api.friendchallenge.application.CheckInWithUserName;
import com.pacefriends.api.friendchallenge.application.RankingView;
import com.pacefriends.api.friendchallenge.domain.FriendChallengeCheckIn;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/friend-challenges/{challengeId}")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping("/check-ins")
    public ResponseEntity<CheckInResponse> registerCheckIn(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID challengeId,
            @Valid @RequestBody CreateCheckInRequest request) {

        FriendChallengeCheckIn checkIn = checkInService.registerCheckIn(
                userId, challengeId,
                request.distanceKm(), request.durationSeconds(),
                request.checkInDate(), request.notes()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CheckInResponse.from(checkIn));
    }

    @GetMapping("/check-ins")
    public ResponseEntity<List<CheckInWithUserNameResponse>> listCheckIns(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID challengeId) {

        List<CheckInWithUserName> checkIns = checkInService.listCheckIns(userId, challengeId);
        List<CheckInWithUserNameResponse> response = checkIns.stream()
                .map(CheckInWithUserNameResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ranking")
    public ResponseEntity<RankingResponse> getRanking(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID challengeId) {

        RankingView ranking = checkInService.getRanking(userId, challengeId);
        return ResponseEntity.ok(RankingResponse.from(ranking));
    }

    @PatchMapping("/check-ins/{checkInId}/reject")
    public ResponseEntity<CheckInResponse> rejectCheckIn(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID challengeId,
            @PathVariable UUID checkInId) {

        FriendChallengeCheckIn checkIn = checkInService.rejectCheckIn(userId, challengeId, checkInId);
        return ResponseEntity.ok(CheckInResponse.from(checkIn));
    }
}
