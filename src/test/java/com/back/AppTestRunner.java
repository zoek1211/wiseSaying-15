package com.back;

import com.back.domain.wiseSaying.repository.WiseSayingDbRepository;
import com.back.standard.util.TestUtil;
import com.back.standard.util.Util;

import java.io.ByteArrayOutputStream;
import java.util.Scanner;

public class AppTestRunner {
    static {
        // 테스트는 wise_saying_dev 대신 wise_saying_test DB(와 db_test/ 폴더)를 사용한다.
        AppConfig.setTestMode();
    }

    // 테스트 전용 저장소를 비운다. 모든 콘솔 테스트가 @BeforeEach 에서 이것만 호출한다.
    // wise_saying_test 의 wise_saying 을 TRUNCATE 하므로 번호도 1부터 다시 시작한다.
    // MySQL 에 연결할 수 없으면 예외가 나서 테스트는 건너뛰지 않고 실패한다.
    public static void resetStorage() {
        AppConfig.setTestMode();
        Util.file.rmdir(AppConfig.getDbDirPath());
        new WiseSayingDbRepository().truncate();
    }

    public static String run(String input) {
        AppConfig.setTestMode();

        Scanner scanner = TestUtil.genScanner(input + "\n종료");

        ByteArrayOutputStream output = TestUtil.setOutToByteArray();
        AppContext.renew(scanner);
        new App().run();
        TestUtil.clearSetOutToByteArray();

        return output.toString();
    }
}
