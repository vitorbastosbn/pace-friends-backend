package com.pacefriends.api.friendchallenge.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendChallengeRepository {
    FriendChallenge save(FriendChallenge challenge);
    Optional<FriendChallenge> findById(UUID id);
    Optional<FriendChallenge> findByInviteCode(String inviteCode);
    boolean existsByInviteCode(String inviteCode);
    List<FriendChallenge> findAllByUserId(UUID userId);
    void updateStatus(UUID id, String status);
    List<FriendChallenge> findArchivedByUserIdPaged(UUID userId, int page, int size);
    long countArchivedByUserId(UUID userId);
}
