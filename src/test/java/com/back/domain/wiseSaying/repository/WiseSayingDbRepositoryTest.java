package com.back.domain.wiseSaying.repository;

import com.back.AppConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class WiseSayingDbRepositoryTest {
    @AfterEach
    void afterEach() {
        AppConfig.setTestMode();
    }

    @Test
    @DisplayName("테스트 모드가 아니면 truncate 는 거부한다 (wise_saying_dev 를 비우지 않는다).")
    void truncateRefusedOutsideTestMode() {
        AppConfig.setProdMode();

        assertThatThrownBy(() -> new WiseSayingDbRepository().truncate())
                .isInstanceOf(IllegalStateException.class);
    }
}
