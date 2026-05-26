package com.pacefriends.api.profile.presentation;

import com.pacefriends.api.profile.application.ProfileService;
import com.pacefriends.api.profile.domain.ProfileData;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {

    private final ProfileService profileService;

    public UserProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<GetProfileResponse> getProfile(
            @AuthenticationPrincipal UUID requestingUserId,
            @PathVariable UUID userId) {

        ProfileData data = profileService.getUserProfile(requestingUserId, userId);
        return ResponseEntity.ok(GetProfileResponse.from(data));
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<GetProfileResponse> updateProfile(
            @AuthenticationPrincipal UUID requestingUserId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateProfileRequest request) {

        ProfileData data = profileService.updateProfile(
                requestingUserId, userId, request.objective(), request.weeklyFrequency());
        return ResponseEntity.ok(GetProfileResponse.from(data));
    }
}
