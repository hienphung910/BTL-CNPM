package com.ptit.oj.dao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DatabaseConfig {
    public static final String ENV_URL = "OJ_DB_URL";
    public static final String ENV_USER = "OJ_DB_USER";
    public static final String ENV_PASSWORD = "OJ_DB_PASSWORD";

    public static final String SAMPLE_URL =
            "jdbc:mysql://localhost:3306/online_judge"
          + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC"
          + "&useSSL=false&allowPublicKeyRetrieval=true";

    private static Map<String, String> dotEnv;

    private String url;
    private String user;
    private String password;

    private DatabaseConfig(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public String getUrl() { return url; }
    public String getUser() { return user; }
    public String getPassword() { return password; }

    public static class MissingConfigException extends RuntimeException {
        public MissingConfigException(String message) {
            super(message);
        }
    }

    public static DatabaseConfig fromEnvironment() {
        String url = value(ENV_URL);
        String user = value(ENV_USER);
        String password = value(ENV_PASSWORD);

        if (isBlank(url) || isBlank(user)) {
            throw new MissingConfigException(
                    "Thiếu cấu hình MySQL để chạy ứng dụng.\n"
                  + "  Hãy đặt biến môi trường " + ENV_URL + " và " + ENV_USER
                  + " (và " + ENV_PASSWORD + " nếu tài khoản có mật khẩu),\n"
                  + "  hoặc chép .env.example thành .env rồi điền vào đó.\n"
                  + "  Ví dụ " + ENV_URL + ":\n    " + SAMPLE_URL);
        }
        return new DatabaseConfig(url.trim(), user.trim(), password == null ? "" : password);
    }

    private static String value(String key) {
        String fromEnv = System.getenv(key);
        if (!isBlank(fromEnv)) return fromEnv;
        String fromProperty = System.getProperty(key);
        if (!isBlank(fromProperty)) return fromProperty;
        return dotEnv().get(key);
    }

    private static Map<String, String> dotEnv() {
        if (dotEnv != null) return dotEnv;
        dotEnv = new LinkedHashMap<>();
        Path file = Path.of(".env");
        if (!Files.isRegularFile(file)) return dotEnv;
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (String raw : lines) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String key = line.substring(0, eq).trim();
                String val = line.substring(eq + 1).trim();
                if (val.length() >= 2
                        && ((val.startsWith("\"") && val.endsWith("\""))
                         || (val.startsWith("'") && val.endsWith("'")))) {
                    val = val.substring(1, val.length() - 1);
                }
                dotEnv.put(key, val);
            }
        } catch (IOException e) {
            System.err.println("Không đọc được file .env: " + e.getMessage());
        }
        return dotEnv;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "DatabaseConfig{user=" + user + ", url=" + MySqlDatabaseManager.maskUrl(url) + "}";
    }
}
