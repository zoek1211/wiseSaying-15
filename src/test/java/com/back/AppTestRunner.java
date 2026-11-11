package com.back;

import com.back.standard.util.TestUtil;
import com.back.standard.util.Util;

import java.io.ByteArrayOutputStream;
import java.util.Scanner;

public class AppTestRunner {
    static {
        // 테스트는 실제 db/ 대신 db_test/ 폴더를 사용한다.
        AppConfig.setTestMode();
    }

    // 테스트 전용 저장소를 비운다. 모든 콘솔 테스트가 @BeforeEach 에서 이것만 호출하므로,
    // 저장소가 바뀌면(폴더 삭제 → TRUNCATE) 여기만 고치면 된다.
    public static void resetStorage() {
        AppConfig.setTestMode();
        Util.file.rmdir(AppConfig.getDbDirPath());
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
