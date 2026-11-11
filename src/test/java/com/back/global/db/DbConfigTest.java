package com.back.global.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DbConfigTest {
    @Test
    @DisplayName("환경변수가 있으면 db.properties 보다 먼저 쓴다.")
    void envWins() {
        assertThat(DbConfig.choose("jdbc:mysql://env", "jdbc:mysql://file")).isEqualTo("jdbc:mysql://env");
    }

    @Test
    @DisplayName("환경변수가 없으면 db.properties 값을 쓴다.")
    void unsetEnvFallsBackToFile() {
        assertThat(DbConfig.choose(null, "jdbc:mysql://file")).isEqualTo("jdbc:mysql://file");
    }

    @Test
    @DisplayName("빈 환경변수는 없는 것으로 보고 db.properties 값을 쓴다.")
    void blankEnvFallsBackToFile() {
        assertThat(DbConfig.choose("", "jdbc:mysql://file")).isEqualTo("jdbc:mysql://file");
        assertThat(DbConfig.choose("   ", "jdbc:mysql://file")).isEqualTo("jdbc:mysql://file");
    }
}
