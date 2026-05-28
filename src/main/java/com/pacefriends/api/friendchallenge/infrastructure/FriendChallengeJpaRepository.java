package com.pacefriends.api.friendchallenge.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface FriendChallengeJpaRepository extends JpaRepository<FriendChallengeEntity, UUID> {
    Optional<FriendChallengeEntity> findByInviteCode(String code);
    boolean existsByInviteCode(String code);

    @Query(value = """
        SELECT fc.* FROM friend_challenges fc
        JOIN friend_challenge_participants fcp ON fcp.friend_challenge_id = fc.id
        WHERE fcp.user_id = :userId
          AND fc.status NOT IN ('ACTIVE', 'DELETED')
        ORDER BY fc.end_date DESC, fc.created_at DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<FriendChallengeEntity> findArchivedByUserId(
        @Param("userId") UUID userId, @Param("limit") int limit, @Param("offset") int offset);

    @Query(value = """
        SELECT COUNT(*) FROM friend_challenges fc
        JOIN friend_challenge_participants fcp ON fcp.friend_challenge_id = fc.id
        WHERE fcp.user_id = :userId
          AND fc.status NOT IN ('ACTIVE', 'DELETED')
        """, nativeQuery = true)
    long countArchivedByUserId(@Param("userId") UUID userId);
}
