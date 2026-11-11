package com.back.global.db;

import com.back.AppConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

// MySQL 접속 정보를 찾는다. 환경변수(DB_URL, DB_USER, DB_PASSWORD)가 먼저이고,
// 없으면 커밋하지 않는 db.properties(db.url, db.user, db.password)를 읽는다.
// 소스에는 비밀번호도 접속 URL 도 두지 않는다. 형식은 db.properties.example 참고.
public class DbConfig {
    private static final String PROPERTIES_FILE_PATH = "db.properties";

    // 운영(prod)은 wise_saying_dev, 테스트는 wise_saying_test 를 쓴다.
    public static String getDbName() {
        return AppConfig.isTestMode() ? "wise_saying_test" : "wise_saying_dev";
    }

    // DB_URL / db.url 은 서버 주소까지만 담는다(예: jdbc:mysql://localhost:3306). DB 이름은 모드가 정한다.
    public static String getUrl() {
        String serverUrl = require("DB_URL", "db.url");

        if (serverUrl.endsWith("/")) serverUrl = serverUrl.substring(0, serverUrl.length() - 1);

        return serverUrl + "/" + getDbName();
    }

    public static String getUser() {
        return require("DB_USER", "db.user");
    }

    public static String getPassword() {
        String password = resolve("DB_PASSWORD", "db.password");

        return password == null ? "" : password;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getUrl(), getUser(), getPassword());
    }

    private static String require(String envName, String propertyName) {
        String value = resolve(envName, propertyName);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "DB 접속 정보가 없습니다. 환경변수 %s 또는 %s 의 %s 를 설정하세요 (db.properties.example 참고)."
                            .formatted(envName, PROPERTIES_FILE_PATH, propertyName)
            );
        }

        return value;
    }

    private static String resolve(String envName, String propertyName) {
        String envValue = System.getenv(envName);

        if (envValue != null) return envValue;

        return loadProperties().getProperty(propertyName);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        Path path = Path.of(PROPERTIES_FILE_PATH);

        if (!Files.exists(path)) return properties;

        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(PROPERTIES_FILE_PATH + " 을 읽을 수 없습니다.", e);
        }

        return properties;
    }
}
