package com.back;

public class AppConfig {
    private static String mode = "prod";

    public static void setProdMode() {
        mode = "prod";
    }

    public static void setTestMode() {
        mode = "test";
    }

    public static boolean isTestMode() {
        return mode.equals("test");
    }

    // 운영: db, 테스트: db_test (실제 db 폴더를 건드리지 않도록 분리)
    public static String getDbDirPath() {
        return isTestMode() ? "db_test" : "db";
    }

    // 빌드(Export) 결과 data.json 의 위치. 저장소 구현과 상관없이 한 곳에서 정한다.
    public static String getExportFilePath() {
        return getDbDirPath() + "/wiseSaying/data.json";
    }
}
