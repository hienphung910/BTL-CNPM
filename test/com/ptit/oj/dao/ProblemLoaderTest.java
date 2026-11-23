package com.ptit.oj.dao;

import com.ptit.oj.model.Problem;
import com.ptit.oj.model.TestCase;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class ProblemLoaderTest {
    private Path tempDir;
    private ProblemLoader instance;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("problems-test-");
        instance = new ProblemLoader(tempDir);
    }

    @AfterEach
    public void tearDown() {
        deleteRecursively(tempDir.toFile());
    }

    @Test
    public void test01_LoadAll_ThuMucThat603Bai() {
        System.out.println("loadAll - thu muc data/problems co 603 bai");
        Path realDir = Paths.get("data", "problems");
        assumeTrue(Files.isDirectory(realDir), "Khong thay data/problems - bo qua test doc thu muc that");
        ProblemLoader realLoader = new ProblemLoader(realDir);

        List<Problem> result = realLoader.loadAll();

        assertEquals(603, result.size());
        assertEquals("K001", result.get(0).getId());
        for (Problem p : result) {
            assertFalse(p.getTestCases().isEmpty());
        }
    }

    @Test
    public void test02_Load_ThuMucHopLe() throws IOException {
        System.out.println("load - thu muc co du problem.properties, statement.txt va 2 cap test");
        Path dir = createProblemDir("P900");
        writeText(dir.resolve("problem.properties"),
                "id=P900\ntitle=Tổng hai số\ntopic=lam-quen\ndifficulty=1\n"
                + "timeLimitMs=1000\nmemoryLimitMb=64\ncomparator=token\ntotalPoints=100\n");
        writeText(dir.resolve("statement.txt"), "Cho hai số a, b. In ra a + b.");
        writeText(dir.resolve("tests").resolve("sample01.in"), "1 2\n");
        writeText(dir.resolve("tests").resolve("sample01.out"), "3\n");
        writeText(dir.resolve("tests").resolve("01.in"), "10 20\n");
        writeText(dir.resolve("tests").resolve("01.out"), "30\n");

        Problem result = instance.load(dir);

        assertEquals("P900", result.getId());
        assertEquals("Tổng hai số", result.getTitle());
        assertEquals("Cho hai số a, b. In ra a + b.", result.getStatement());
        assertEquals(1000, result.getTimeLimitMs());
        assertEquals(64, result.getMemoryLimitMb());
        assertEquals("lam-quen", result.getTopic());
        assertEquals(1, result.getDifficulty());

        List<TestCase> tests = result.getTestCases();
        assertEquals(2, tests.size());
        assertEquals("sample01", tests.get(0).getId());
        assertTrue(tests.get(0).isSample());
        assertEquals("01", tests.get(1).getId());
        assertFalse(tests.get(1).isSample());
        assertEquals(50.0, tests.get(0).getPoints(), 0.001);
        assertEquals(100.0, result.getMaxPoints(), 0.001);
    }

    @Test
    public void test03_Load_ThieuTep() throws IOException {
        System.out.println("load - thieu problem.properties hoac thieu file .out");

        Path noConfig = createProblemDir("P901");
        writeText(noConfig.resolve("tests").resolve("01.in"), "1\n");
        writeText(noConfig.resolve("tests").resolve("01.out"), "1\n");
        try {
            instance.load(noConfig);
            fail("Thieu problem.properties phai nem ProblemLoadException");
        } catch (ProblemLoadException expected) {
            assertTrue(expected.getMessage().contains("problem.properties"));
        }

        Path noOutput = createProblemDir("P902");
        writeText(noOutput.resolve("problem.properties"), "id=P902\ntitle=Thiếu đáp án\n");
        writeText(noOutput.resolve("tests").resolve("01.in"), "1\n");
        try {
            instance.load(noOutput);
            fail("Thieu file .out phai nem ProblemLoadException");
        } catch (ProblemLoadException expected) {
            assertTrue(expected.getMessage().contains("01.in"));
        }
    }

    private Path createProblemDir(String id) throws IOException {
        Path dir = tempDir.resolve(id);
        Files.createDirectories(dir.resolve("tests"));
        return dir;
    }

    private void writeText(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }

    private void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }
}
