/*
 * Kiem thu don vi (JUnit 5) cho MySqlSubmissionRepository va MySqlUserRepository - Module 4.
 */
package com.ptit.oj.dao;

import com.ptit.oj.model.Submission;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Chay 10 test cua bang 6.1 tren MySQL that.
 *
 * Test chay tren MOT CSDL RIENG cho kiem thu (vi du online_judge_test), doc tu 3 bien
 * sau - bien moi truong, -D cua JVM hoac dong KEY=VALUE trong file .env o thu muc goc:
 *
 *   OJ_TEST_DB_URL       jdbc:mysql://localhost:3306/online_judge_test?...
 *   OJ_TEST_DB_USER      oj_app
 *   OJ_TEST_DB_PASSWORD  (mat khau)
 *
 * Truoc moi test, bang submissions cua CSDL kiem thu duoc XOA HET de cac truong hop
 * "chua co bai nop nao" dung nghia. Vi vay lop nay chi chay khi ten CSDL trong
 * OJ_TEST_DB_URL co chu "test"; neu khong thi ca lop duoc bo qua (skipped), khong bao loi.
 *
 * @author Thai Duy Manh
 */
public class MySqlSubmissionRepositoryTest extends SubmissionRepositoryTestBase {

    private static MySqlDatabaseManager database;

    public MySqlSubmissionRepositoryTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        String url = setting("OJ_TEST_DB_URL");
        assumeTrue(url != null && !url.trim().isEmpty(),
                "Chua dat OJ_TEST_DB_URL - bo qua kiem thu tren MySQL");
        assumeTrue(url.toLowerCase(Locale.ROOT).contains("test"),
                "OJ_TEST_DB_URL khong tro toi CSDL kiem thu (ten phai co chu test) - bo qua de khong xoa du lieu that");
        database = new MySqlDatabaseManager(url, setting("OJ_TEST_DB_USER"), setting("OJ_TEST_DB_PASSWORD"));
        database.testConnection();
        new MySqlSchemaInitializer(database).initialize();
        System.out.println("=== Kiem thu MySqlSubmissionRepository tren " + database.getSafeUrl() + " ===");
    }

    @AfterAll
    public static void tearDownClass() {
        database = null;
    }

    @Override
    protected void createRepositories() {
        users = new MySqlUserRepository(database);
        instance = new MySqlSubmissionRepository(database, problems, users, languages);

        // Don bang submissions cua CSDL kiem thu; test_case_results bi xoa theo (ON DELETE CASCADE).
        for (Submission old : instance.findAll()) {
            instance.deleteById(old.getId());
        }
    }

    /** Bien moi truong truoc, roi den -Dkey=value, cuoi cung la file .env. */
    private static String setting(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) value = System.getProperty(key);
        if (value == null || value.trim().isEmpty()) value = fromDotEnv(key);
        return value;
    }

    private static String fromDotEnv(String key) {
        Path file = Paths.get(".env");
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
