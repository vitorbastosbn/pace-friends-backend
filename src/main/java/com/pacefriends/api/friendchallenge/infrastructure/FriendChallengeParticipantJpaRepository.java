package com.pacefriends.api.friendchallenge.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendChallengeParticipantJpaRepository extends JpaRepository<FriendChallengeParticipantEntity, UUID> {
    List<FriendChallengeParticipantEntity> findAllByFriendChallengeId(UUID friendChallengeId);
    List<FriendChallengeParticipantEntity> findAllByUserId(UUID userId);
    boolean existsByFriendChallengeIdAndUserId(UUID friendChallengeId, UUID userId);
    int countByFriendChallengeId(UUID friendChallengeId);
    Optional<FriendChallengeParticipantEntity> findByFriendChallengeIdAndUserId(UUID friendChallengeId, UUID userId);
    void deleteByFriendChallengeIdAndUserId(UUID friendChallengeId, UUID userId);

    @Query(value = """
            SELECT COUNT(DISTINCT fc.id)
            FROM friend_challenges fc
            JOIN friend_challenge_participants fcp ON fcp.friend_challenge_id = fc.id AND fcp.user_id = :userId
            WHERE fc.status = 'FINISHED'
              AND EXISTS (
                SELECT 1 FROM friend_challenge_check_ins ci0
                WHERE ci0.friend_challenge_id = fc.id AND ci0.status = 'VALID'
              )
              AND COALESCE((
                SELECT SUM(ci.distance_km)
                FROM friend_challenge_check_ins ci
                WHERE ci.friend_challenge_id = fc.id AND ci.user_id = :userId AND ci.status = 'VALID'
              ), 0) >= (
                SELECT MAX(user_total) FROM (
                  SELECT SUM(ci2.distance_km) AS user_total
                  FROM friend_challenge_check_ins ci2
                  WHERE ci2.friend_challenge_id = fc.id AND ci2.status = 'VALID'
                  GROUP BY ci2.user_id
                ) t
              )
            """, nativeQuery = true)
    int countVictoriesForUser(@Param("userId") UUID userId);
}
