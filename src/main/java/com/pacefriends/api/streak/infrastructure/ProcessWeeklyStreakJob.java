package com.pacefriends.api.streak.infrastructure;

import com.pacefriends.api.streak.application.ProcessWeeklyStreakService;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
public class ProcessWeeklyStreakJob {

    private static final Logger log = LoggerFactory.getLogger(ProcessWeeklyStreakJob.class);
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private final UserRepository userRepository;
    private final ProcessWeeklyStreakService processWeeklyStreakService;
    private final Clock clock;

    @Autowired
    public ProcessWeeklyStreakJob(UserRepository userRepository,
                                  ProcessWeeklyStreakService processWeeklyStreakService) {
        this(userRepository, processWeeklyStreakService, Clock.system(ZONE));
    }

    ProcessWeeklyStreakJob(UserRepository userRepository,
                           ProcessWeeklyStreakService processWeeklyStreakService,
                           Clock clock) {
        this.userRepository = userRepository;
        this.processWeeklyStreakService = processWeeklyStreakService;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 0 * * SUN", zone = "America/Sao_Paulo")
    public void run() {
        LocalDate today = LocalDate.now(clock);
        log.info("Processing weekly streaks for date={}", today);

        List<User> users = userRepository.findAll();
        for (User user : users) {
            try {
                processWeeklyStreakService.process(user.getId(), today);
            } catch (Exception e) {
                log.error("Failed to process streak for userId={}", user.getId(), e);
            }
        }

        log.info("Weekly streak processing complete. Processed {} users.", users.size());
    }
}
