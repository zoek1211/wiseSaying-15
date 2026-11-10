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

    // 테스트 전용 저장 폴더를 비운다. 매 테스트 전에 호출한다.
    public static void clearDb() {
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
