package com.ptit.oj.dao;

import com.ptit.oj.model.Topic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TopicLoaderTest {
    private Path tempFile;

    @BeforeEach
    public void setUp() throws IOException {
        tempFile = Files.createTempFile("topics-test-", ".txt");
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    public void test01_LoadAll_TepThat36ChuDe() {
        System.out.println("loadAll - tep data/topics.txt co 36 chu de");
        Path realFile = Paths.get("data", "topics.txt");
        assumeTrue(Files.isRegularFile(realFile), "Khong thay data/topics.txt - bo qua test doc tep that");
        TopicLoader instance = new TopicLoader(realFile);

        List<Topic> result = instance.loadAll();

        assertEquals(36, result.size());
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < result.size(); i++) {
            assertEquals(i + 1, result.get(i).getOrder());
            ids.add(result.get(i).getId());
        }
        assertEquals(36, ids.size());
    }

    @Test
    public void test02_LoadAll_BoQuaDongTrongVaGhiChu() throws IOException {
        System.out.println("loadAll - bo qua dong trong va dong ghi chu");
        writeTempFile("# Lo trinh mau\n"
                + "\n"
                + "lam-quen | Làm quen: Nhập và xuất | Đọc và in dữ liệu | Nhập môn\n"
                + "# ghi chu o giua\n"
                + "\n"
                + "vong-lap | Vòng lặp | Lặp có điều kiện | Nhập môn\n"
                + "mang | Mảng (danh sách)\n");
        TopicLoader instance = new TopicLoader(tempFile);

        List<Topic> result = instance.loadAll();

        assertEquals(3, result.size());
        assertEquals("lam-quen", result.get(0).getId());
        assertEquals("Làm quen: Nhập và xuất", result.get(0).getName());
        assertEquals("Nhập môn", result.get(0).getGroup());
        assertEquals(1, result.get(0).getOrder());
        assertEquals("vong-lap", result.get(1).getId());
        assertEquals(2, result.get(1).getOrder());
        assertEquals("mang", result.get(2).getId());
        assertEquals(3, result.get(2).getOrder());
        assertEquals("", result.get(2).getDescription());
    }

    @Test
    public void test03_LoadAll_MaTrungHoacThieuTen() throws IOException {
        System.out.println("loadAll - ma chu de bi trung hoac thieu ten");

        writeTempFile("vong-lap | Vòng lặp\n" + "vong-lap | Vòng lặp lần hai\n");
        try {
            new TopicLoader(tempFile).loadAll();
            fail("Ma chu de bi trung phai nem ProblemLoadException");
        } catch (ProblemLoadException expected) {
            assertTrue(expected.getMessage().contains("vong-lap"));
        }

        writeTempFile("mang |   \n");
        try {
            new TopicLoader(tempFile).loadAll();
            fail("Thieu ten chu de phai nem ProblemLoadException");
        } catch (ProblemLoadException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    private void writeTempFile(String content) throws IOException {
        Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));
    }
}
