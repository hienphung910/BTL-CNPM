/*
 * Kiem thu don vi (JUnit 5) cho viec luu bai nop tren MySqlSubmissionRepository - Module 3.
 */
package com.ptit.oj.dao;

import com.ptit.oj.model.JudgeResult;
import com.ptit.oj.model.Submission;
import com.ptit.oj.model.TestCase;
import com.ptit.oj.model.TestCaseResult;
import com.ptit.oj.model.Verdict;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Chay 8 test cua bang 6.1 tren MySQL that: 7 test dung chung va test06 ve transaction.
 *
 * Test chay tren MOT CSDL RIENG cho kiem thu (vi du online_judge_test), doc tu 3 bien
 * sau - bien moi truong, -D cua JVM hoac dong KEY=VALUE trong file .env o thu muc goc:
 *
 *   OJ_TEST_DB_URL       jdbc:mysql://localhost:3306/online_judge_test?...
 *   OJ_TEST_DB_USER      oj_app
 *   OJ_TEST_DB_PASSWORD  (mat khau)
 *
 * Lop nay chi chay khi ten CSDL trong OJ_TEST_DB_URL co chu "test"; neu khong thi ca lop
 * duoc bo qua (skipped), khong bao loi. Moi test chi them roi xoa du lieu cua rieng no.
 */
public class MySqlSubmissionSaveTest extends SubmissionSaveTestBase {

    private static MySqlDatabaseManager database;

    public MySqlSubmissionSaveTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        String url = setting("OJ_TEST_DB_URL");
        assumeTrue(url != null && !url.trim().isEmpty(),
                "Chua dat OJ_TEST_DB_URL - bo qua kiem thu tren MySQL");
        assumeTrue(url.toLowerCase(Locale.ROOT).contains("test"),
                "OJ_TEST_DB_URL khong tro toi CSDL kiem thu (ten phai co chu test) - bo qua");
        database = new MySqlDatabaseManager(url, setting("OJ_TEST_DB_USER"), setting("OJ_TEST_DB_PASSWORD"));
        database.testConnection();
        new MySqlSchemaInitializer(database).initialize();
        System.out.println("=== Kiem thu luu bai nop tren MySqlSubmissionRepository, " + database.getSafeUrl() + " ===");
    }

    @AfterAll
    public static void tearDownClass() {
        database = null;
    }

    @Override
    protected void createRepositories() {
        users = new MySqlUserRepository(database);
        instance = new MySqlSubmissionRepository(database, problems, users, languages);
    }

    /**
     * Test of saveWithResults method, of class SubmissionRepository.
     * Loi khi dang ghi ket qua test: khong dong nao duoc luu, vi bai nop va ket qua test
     * nam trong cung mot transaction.
     *
     * Cach gay loi: ket qua test thu ba mang ma test dai 150 ky tu, trong khi cot
     * test_case_id chi chua duoc 100 ky tu, nen MySQL (che do STRICT mac dinh) tu choi dong do.
     * Luc nay dong submissions va hai ket qua test dau da duoc gui di nhung chua commit.
     */
    @Test
    public void test06_SaveWithResults_LoiKhiGhiKetQuaTest() {
        System.out.println("saveWithResults - loi khi dang ghi ket qua test");
        int before = instance.count();
        Submission submission = createSubmission();
        createdSubmissionIds.add(submission.getId());     // de tearDown() don neu test nay sai

        StringBuilder longId = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            longId.append('x');
        }
        List<TestCase> tests = k001.getTestCases();
        JudgeResult result = new JudgeResult(submission);
        result.addTestResult(new TestCaseResult(tests.get(0), Verdict.AC, 8, tests.get(0).getPoints(), ""));
        result.addTestResult(new TestCaseResult(tests.get(1), Verdict.AC, 8, tests.get(1).getPoints(), ""));
        result.addTestResult(new TestCaseResult(
                new TestCase(longId.toString(), "", "", 12.5, false), Verdict.AC, 8, 12.5, ""));
        submission.setResult(result);

        try {
            instance.saveWithResults(submission);
            fail("Loi khi ghi ket qua test phai nem DataAccessException");
        } catch (DataAccessException expected) {
            assertTrue(expected.getMessage().contains(submission.getId()));
        }

        // Rollback: khong co bai nop, cung khong co ket qua test nao duoc luu.
        assertNull(instance.findById(submission.getId()));
        assertEquals(before, instance.count());
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
