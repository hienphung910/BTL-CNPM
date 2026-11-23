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

    @Test
    public void test06_SaveWithResults_LoiKhiGhiKetQuaTest() {
        System.out.println("saveWithResults - loi khi dang ghi ket qua test");
        int before = instance.count();
        Submission submission = createSubmission();
        createdSubmissionIds.add(submission.getId());

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

        assertNull(instance.findById(submission.getId()));
        assertEquals(before, instance.count());
    }

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
