package com.pacefriends.api.friendchallenge.application;

import com.pacefriends.api.friendchallenge.domain.ChallengeType;
import com.pacefriends.api.friendchallenge.domain.FriendChallenge;
import com.pacefriends.api.friendchallenge.domain.FriendChallengeCheckIn;
import com.pacefriends.api.friendchallenge.domain.FriendChallengeCheckInRepository;
import com.pacefriends.api.friendchallenge.domain.exception.FriendChallengeNotFoundException;
import com.pacefriends.api.friendchallenge.domain.exception.InvalidInviteCodeException;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FriendChallengeLifecycleIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FriendChallengeService friendChallengeService;
    @Autowired
    private CheckInService checkInService;
    @Autowired
    private FriendChallengeCheckInRepository checkInRepository;

    @Test
    void memberLeaveRejoinAndCreatorDelete_keepRemovedHistoryOutOfProductAccess() {
        User creator = saveUser("creator");
        User member = saveUser("member");
        FriendChallenge created = friendChallengeService.createChallenge(
                creator.getId(),
                "Corrida dos amigos",
                null,
                ChallengeType.DISTANCE,
                BigDecimal.valueOf(20),
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );

        friendChallengeService.joinChallenge(member.getId(), created.inviteCode());
        FriendChallengeCheckIn checkIn = checkInService.registerCheckIn(
                member.getId(), created.id(), 5.0, 1800, LocalDate.now(), null);

        friendChallengeService.leaveChallenge(member.getId(), created.id());

        assertThat(checkInRepository.findById(checkIn.id()).orElseThrow().status())
                .isEqualTo(FriendChallengeCheckIn.STATUS_REMOVED_BY_LEAVE);
        assertThat(checkInService.getRanking(creator.getId(), created.id()).entries()).isEmpty();

        friendChallengeService.joinChallenge(member.getId(), created.inviteCode());
        assertThat(checkInService.getRanking(member.getId(), created.id()).entries()).isEmpty();

        friendChallengeService.deleteChallenge(creator.getId(), created.id());

        assertThat(checkInRepository.findById(checkIn.id()).orElseThrow().status())
                .isEqualTo(FriendChallengeCheckIn.STATUS_REMOVED_BY_DELETE);
        assertThat(friendChallengeService.listChallenges(creator.getId())).isEmpty();
        assertThatThrownBy(() -> friendChallengeService.getChallengeDetail(creator.getId(), created.id()))
                .isInstanceOf(FriendChallengeNotFoundException.class);
        assertThatThrownBy(() -> checkInService.getRanking(creator.getId(), created.id()))
                .isInstanceOf(FriendChallengeNotFoundException.class);
        assertThatThrownBy(() -> friendChallengeService.joinChallenge(member.getId(), created.inviteCode()))
                .isInstanceOf(InvalidInviteCodeException.class);
        assertThatThrownBy(() -> friendChallengeService.deleteChallenge(creator.getId(), created.id()))
                .isInstanceOf(FriendChallengeNotFoundException.class);
    }

    private User saveUser(String label) {
        return userRepository.save(User.builder()
                .googleId(label + "-google")
                .email(label + "@example.com")
                .name(label)
                .build());
    }
}
