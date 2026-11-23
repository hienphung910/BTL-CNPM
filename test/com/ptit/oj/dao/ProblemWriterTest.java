/*
 * Kiem thu don vi (JUnit 5) cho ProblemWriter - Module 2: Quan ly bai tap va lo trinh hoc.
 * Moi phuong thuc test ung voi mot dong trong bang 6.1 cua bao cao, dung thu tu.
 */
package com.ptit.oj.dao;

import com.ptit.oj.model.Problem;
import com.ptit.oj.model.ProblemDraft;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiem thu viec ghi bai tap moi xuong o dia va sua tep problem.properties cua bai da co.
 * Moi test lam viec trong mot thu muc tam rieng, xong thi xoa di, khong dung toi data/problems.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class ProblemWriterTest {

    private Path tempDir;
    private ProblemWriter instance;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("problem-writer-test-");
        instance = new ProblemWriter(tempDir);
    }

    @AfterEach
    public void tearDown() {
        deleteRecursively(tempDir.toFile());
    }

    // ================================================================
    // 2. Tao bai tap - exists(), write()
    // ================================================================

    /**
     * Test of exists method, of class ProblemWriter.
     * Ma bai da co thu muc: tra ve true; ma bai chua co: tra ve false.
     */
    @Test
    public void test01_Exists() throws IOException {
        System.out.println("exists - ma bai da co thu muc va ma bai chua co");
        Files.createDirectory(tempDir.resolve("P900"));

        assertTrue(instance.exists("P900"));
        assertFalse(instance.exists("P901"));
    }

    /**
     * Test of write method, of class ProblemWriter.
     * Ban thao hop le: tao thu muc <id> gom problem.properties, statement.txt va
     * tests/<ten>.in, .out cho tung test; khong con thu muc tam.
     */
    @Test
    public void test02_Write_BanThaoHopLe() throws IOException {
        System.out.println("write - ban thao hop le");
        ProblemDraft draft = createDraft("P900", "Tổng hai số");

        Path result = instance.write(draft);

        assertEquals(tempDir.resolve("P900"), result);
        assertTrue(Files.isRegularFile(result.resolve("problem.properties")));
        assertTrue(Files.isRegularFile(result.resolve("statement.txt")));
        assertEquals("1 2\n", readText(result.resolve("tests").resolve("sample01.in")));
        assertEquals("3\n", readText(result.resolve("tests").resolve("sample01.out")));
        assertEquals("10 20\n", readText(result.resolve("tests").resolve("01.in")));
        assertEquals("30\n", readText(result.resolve("tests").resolve("01.out")));

        // Khong con thu muc tam: trong thu muc goc chi co dung thu muc P900.
        File[] children = tempDir.toFile().listFiles();
        assertEquals(1, children.length);
        assertEquals("P900", children[0].getName());

        // Bai vua ghi doc lai duoc bang ProblemLoader, dung noi dung da nhap.
        Problem loaded = new ProblemLoader(tempDir).load(result);
        assertEquals("P900", loaded.getId());
        assertEquals("Tổng hai số", loaded.getTitle());
        assertEquals("Cho hai số a, b. In ra a + b.", loaded.getStatement());
        assertEquals("lam-quen", loaded.getTopic());
        assertEquals(1, loaded.getDifficulty());
        assertEquals(2, loaded.getTestCases().size());
        assertTrue(loaded.getTestCases().get(0).isSample());
    }

    /**
     * Test of write method, of class ProblemWriter.
     * Thu muc da ton tai: nem IOException, khong ghi de bai cu.
     */
    @Test
    public void test03_Write_ThuMucDaTonTai() throws IOException {
        System.out.println("write - thu muc bai da ton tai");
        instance.write(createDraft("P900", "Tổng hai số"));
        String before = readText(tempDir.resolve("P900").resolve("problem.properties"));

        try {
            instance.write(createDraft("P900", "Tên khác hẳn"));
            fail("Thu muc bai da ton tai phai nem IOException");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("P900"));
        }

        // Bai cu con nguyen va khong sot lai thu muc tam nao.
        assertEquals(before, readText(tempDir.resolve("P900").resolve("problem.properties")));
        assertEquals(1, tempDir.toFile().listFiles().length);
    }

    // ================================================================
    // 3. Sua bai trong lo trinh - updateProperties()
    // ================================================================

    /**
     * Test of updateProperties method, of class ProblemWriter.
     * Doi difficulty tu 1 thanh 2: dong difficulty duoc ghi lai, cac dong khac giu nguyen
     * thu tu va noi dung.
     */
    @Test
    public void test04_UpdateProperties_DoiDoKho() throws IOException {
        System.out.println("updateProperties - doi difficulty tu 1 thanh 2");
        Path file = createPropertiesFile("P900",
                "# Bài mẫu\nid=P900\ntitle=Tổng hai số\ntopic=lam-quen\ndifficulty=1\norder=3\n");
        Map<String, String> changes = new HashMap<>();
        changes.put("difficulty", "2");

        Path result = instance.updateProperties("P900", changes);

        assertEquals(tempDir.resolve("P900"), result);
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals(6, lines.size());
        assertEquals("# Bài mẫu", lines.get(0));
        assertEquals("id=P900", lines.get(1));
        assertEquals("title=Tổng hai số", lines.get(2));
        assertEquals("topic=lam-quen", lines.get(3));
        assertEquals("difficulty=2", lines.get(4));
        assertEquals("order=3", lines.get(5));
        // Khong sot lai tep tam trong thu muc bai.
        assertEquals(1, result.toFile().listFiles().length);
    }

    /**
     * Test of updateProperties method, of class ProblemWriter.
     * changes co gia tri null cho khoa order: dong order bi xoa khoi tep.
     */
    @Test
    public void test05_UpdateProperties_XoaDongOrder() throws IOException {
        System.out.println("updateProperties - gia tri null thi xoa dong");
        Path file = createPropertiesFile("P900",
                "id=P900\ntitle=Tổng hai số\ntopic=lam-quen\ndifficulty=1\norder=3\n");
        Map<String, String> changes = new HashMap<>();
        changes.put("order", null);

        instance.updateProperties("P900", changes);

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals(4, lines.size());
        assertEquals("id=P900", lines.get(0));
        assertEquals("title=Tổng hai số", lines.get(1));
        assertEquals("topic=lam-quen", lines.get(2));
        assertEquals("difficulty=1", lines.get(3));
    }

    /**
     * Test of updateProperties method, of class ProblemWriter.
     * Ma bai khong co thu muc: nem IOException.
     */
    @Test
    public void test06_UpdateProperties_MaBaiKhongCoThuMuc() {
        System.out.println("updateProperties - ma bai khong co thu muc");
        Map<String, String> changes = new HashMap<>();
        changes.put("difficulty", "2");

        try {
            instance.updateProperties("P999", changes);
            fail("Ma bai khong co thu muc phai nem IOException");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("P999"));
        }
        assertFalse(instance.exists("P999"));     // khong tu tao thu muc moi
    }

    // ================================================================
    // Du lieu mau
    // ================================================================

    /** Ban thao mot bai co 2 test: sample01 la test vi du, 01 la test an. */
    private ProblemDraft createDraft(String id, String title) {
        ProblemDraft draft = new ProblemDraft();
        draft.setId(id);
        draft.setTitle(title);
        draft.setStatement("Cho hai số a, b. In ra a + b.");
        draft.setTimeLimitMs(1000);
        draft.setMemoryLimitMb(64);
        draft.setComparator("token");
        draft.setTotalPoints(100);
        draft.setTopic("lam-quen");
        draft.setDifficulty(1);
        draft.addTest(new ProblemDraft.TestDraft("sample01", "1 2\n", "3\n", true));
        draft.addTest(new ProblemDraft.TestDraft("01", "10 20\n", "30\n", false));
        return draft;
    }

    /** Tao thu muc bai chi co tep problem.properties voi noi dung cho truoc. */
    private Path createPropertiesFile(String id, String content) throws IOException {
        Path dir = tempDir.resolve(id);
        Files.createDirectory(dir);
        Path file = dir.resolve("problem.properties");
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    private String readText(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
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
