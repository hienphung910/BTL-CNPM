/*
 * Kiem thu don vi (JUnit 5) cho MySqlUserRepository - Module 1.
 */
package com.ptit.oj.dao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Chay 12 test cua bang 6.1 tren MySQL that.
 *
 * De khong lam hong du lieu dang dung, test chay tren MOT CSDL RIENG cho kiem thu
 * (vi du online_judge_test), doc tu 3 bien sau - bien moi truong, -D cua JVM
 * hoac dong KEY=VALUE trong file .env o thu muc goc:
 *
 *   OJ_TEST_DB_URL       jdbc:mysql://localhost:3306/online_judge_test?...
 *   OJ_TEST_DB_USER      oj_app
 *   OJ_TEST_DB_PASSWORD  (mat khau)
 *
 * Thieu OJ_TEST_DB_URL thi ca lop duoc bo qua (skipped), khong bao loi.
 * Bang users duoc tao neu chua co; 5 tai khoan mau duoc them neu chua co;
 * tai khoan do test them vao (ducdm) bi xoa o tearDown().
 *
 * @author Phung Minh Hien
 */
public class MySqlUserRepositoryTest extends UserRepositoryTestBase {

    private static MySqlDatabaseManager database;

    public MySqlUserRepositoryTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        String url = setting("OJ_TEST_DB_URL");
        assumeTrue(url != null && !url.trim().isEmpty(),
                "Chua dat OJ_TEST_DB_URL - bo qua kiem thu tren MySQL");
        database = new MySqlDatabaseManager(url, setting("OJ_TEST_DB_USER"), setting("OJ_TEST_DB_PASSWORD"));
        database.testConnection();
        new MySqlSchemaInitializer(database).initialize();
        System.out.println("=== Kiem thu MySqlUserRepository tren " + database.getSafeUrl() + " ===");
    }

    @AfterAll
    public static void tearDownClass() {
        database = null;
    }

    @Override
    protected UserRepository createRepository() {
        return new MySqlUserRepository(database);
    }

    /** Bien moi truong truoc, roi den -Dkey=value, cuoi cung la file .env. */
    private static String setting(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) value = System.getProperty(key);
        if (value == null || value.trim().isEmpty()) value = fromDotEnv(key);
        return value;
    }

    private static String fromDotEnv(String key) {
        Path file = Path.of(".env");
        if (!Files.isRegularFile(file)) return null;
        try {
            for (String raw : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String line = raw.trim();
                if (line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq > 0 && line.substring(0, eq).trim().equals(key)) {
                    return line.substring(eq + 1).trim();
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
}
