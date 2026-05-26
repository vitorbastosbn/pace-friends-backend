package com.pacefriends.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaceFriendsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaceFriendsApplication.class, args);
    }
}
