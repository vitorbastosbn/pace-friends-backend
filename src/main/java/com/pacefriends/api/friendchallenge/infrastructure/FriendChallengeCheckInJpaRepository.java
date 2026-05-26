package com.pacefriends.api.friendchallenge.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FriendChallengeCheckInJpaRepository extends JpaRepository<FriendChallengeCheckInEntity, UUID> {
    List<FriendChallengeCheckInEntity> findAllByFriendChallengeId(UUID friendChallengeId);
    boolean existsByFriendChallengeIdAndUserIdAndCheckInDateAndStatus(
            UUID friendChallengeId, UUID userId, LocalDate checkInDate, String status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE FriendChallengeCheckInEntity c SET c.status = :status
            WHERE c.friendChallengeId = :challengeId AND c.userId = :userId
            """)
    void updateStatusByChallengeIdAndUserId(@Param("challengeId") UUID challengeId,
                                            @Param("userId") UUID userId,
                                            @Param("status") String status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE FriendChallengeCheckInEntity c SET c.status = :status
            WHERE c.friendChallengeId = :challengeId
            """)
    void updateStatusByChallengeId(@Param("challengeId") UUID challengeId,
                                   @Param("status") String status);
}
