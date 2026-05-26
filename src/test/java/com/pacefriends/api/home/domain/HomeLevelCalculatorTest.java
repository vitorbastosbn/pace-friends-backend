package com.pacefriends.api.home.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class HomeLevelCalculatorTest {

    @ParameterizedTest
    @CsvSource(value = {
            "0,1,100",
            "99,1,100",
            "100,2,300",
            "299,2,300",
            "300,3,600",
            "599,3,600",
            "600,4,1000",
            "999,4,1000",
            "1000,5,1500",
            "1499,5,1500",
            "1500,6,NULL"
    }, nullValues = "NULL")
    void fromTotalXp_returnsLevelAndNextThreshold(int xp, int expectedLevel,
                                                   Integer expectedNextThreshold) {
        HomeSummary.Level level = HomeLevelCalculator.fromTotalXp(xp);

        assertThat(level.current()).isEqualTo(expectedLevel);
        assertThat(level.xpForNextLevel()).isEqualTo(expectedNextThreshold);
    }
}
