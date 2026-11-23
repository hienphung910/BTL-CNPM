/*
 * Kiem thu don vi (JUnit 5) cho tang DAO cua Module 4 - Xem lich su nop bai va bang xep hang.
 * Moi phuong thuc test ung voi mot dong trong bang 6.1 cua bao cao, dung thu tu.
 */
package com.ptit.oj.dao;

import com.ptit.oj.controller.judge.Language;
import com.ptit.oj.controller.judge.LanguageRegistry;
import com.ptit.oj.model.Credentials;
import com.ptit.oj.model.JudgeResult;
import com.ptit.oj.model.Problem;
import com.ptit.oj.model.Student;
import com.ptit.oj.model.Submission;
import com.ptit.oj.model.TestCase;
import com.ptit.oj.model.TestCaseResult;
import com.ptit.oj.model.User;
import com.ptit.oj.model.Verdict;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Cac test dung chung cho moi cach luu bai nop (bo nho hay MySQL).
 * Lop con chi can tao hai kho du lieu: kho bai nop va kho tai khoan.
 *
 * Moi test tu tao tai khoan va bai nop cua rieng minh, xong thi xoa o tearDown(),
 * nen cac test khong anh huong lan nhau.
 *
 * @author Thai Duy Manh
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class SubmissionRepositoryTestBase {

    /** Moc thoi gian cua cac bai nop mau; bai nop sau cach bai truoc mot giay. */
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 11, 17, 8, 54, 30);

    /** Kho bai nop can kiem thu. */
    protected SubmissionRepository instance;

    /** Kho tai khoan di cung, dung cho solvedCountByUser(). */
    protected UserRepository users;

    /** Kho bai tap va danh sach ngon ngu: bai tap khong nam trong CSDL nen luon o trong bo nho. */
    protected Repository<Problem> problems;
    protected LanguageRegistry languages;

    private Problem k001;
    private Problem k002;
    private Language cpp;

    private User hien;
    private User lan;
    private User tuan;

    /** Ma bai nop va ma tai khoan do test tao ra, xoa o tearDown(). */
    private List<String> createdSubmissionIds = new ArrayList<>();
    private List<String> createdUserIds = new ArrayList<>();

    /** So bai nop da tao, dung de moi bai nop co mot thoi diem nop rieng. */
    private int submissionCount = 0;

    /** Lop con gan gia tri cho instance va users (duoc dung problems va languages). */
    protected abstract void createRepositories();

    @BeforeEach
    public void setUp() {
        problems = new InMemoryRepository<>();
        languages = new LanguageRegistry();
        k001 = createProblem("K001", "Con vẹt Lóc Cóc nhại số", 8);
        k002 = createProblem("K002", "Robot Bi chào bạn mới", 4);
        cpp = languages.byName("C++");

        createRepositories();

        hien = createStudent("Nguyễn Minh Hiển", "B24DCCN199");
        lan = createStudent("Trần Lan Anh", "B24DCCN042");
        tuan = createStudent("Phạm Văn Tuấn", "B24DCCN311");
    }

    @AfterEach
    public void tearDown() {
        for (String id : createdSubmissionIds) {
            instance.deleteById(id);
        }
        for (String id : createdUserIds) {
            users.deleteById(id);
        }
        createdSubmissionIds.clear();
        createdUserIds.clear();
    }

    // ================================================================
    // 1. Xem lich su nop bai - findAllNewestFirst(), findByUserId()
    // ================================================================

    /**
     * Test of findAllNewestFirst method, of class SubmissionRepository.
     * Chua co bai nop nao: tra ve danh sach rong.
     */
    @Test
    public void test01_FindAllNewestFirst_ChuaCoBaiNop() {
        System.out.println("findAllNewestFirst - chua co bai nop nao");
        List<Submission> result = instance.findAllNewestFirst();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * Test of findAllNewestFirst method, of class SubmissionRepository.
     * Co nhieu bai nop: tra ve du so bai, bai moi nhat dung dau.
     */
    @Test
    public void test02_FindAllNewestFirst_NhieuBaiNop() {
        System.out.println("findAllNewestFirst - co nhieu bai nop");
        Submission first = saveSubmission(tuan, k001, Verdict.AC);
        Submission second = saveSubmission(lan, k001, Verdict.WA);
        Submission third = saveSubmission(hien, k001, Verdict.AC);

        List<Submission> result = instance.findAllNewestFirst();

        assertEquals(3, result.size());
        assertEquals(third.getId(), result.get(0).getId());
        assertEquals(second.getId(), result.get(1).getId());
        assertEquals(first.getId(), result.get(2).getId());
    }

    /**
     * Test of findByUserId method, of class SubmissionRepository.
     * Nguoi dung co bai nop: chi tra ve cac bai cua nguoi do.
     */
    @Test
    public void test03_FindByUserId_CoBaiNop() {
        System.out.println("findByUserId - nguoi dung co bai nop (hiennm)");
        Submission older = saveSubmission(hien, k001, Verdict.WA);
        saveSubmission(lan, k001, Verdict.AC);
        Submission newer = saveSubmission(hien, k002, Verdict.AC);

        List<Submission> result = instance.findByUserId(hien.getId());

        assertEquals(2, result.size());
        for (Submission s : result) {
            assertEquals(hien.getId(), s.getAuthor().getId());
        }
        assertEquals(newer.getId(), result.get(0).getId());
        assertEquals(older.getId(), result.get(1).getId());
    }

    /**
     * Test of findByUserId method, of class SubmissionRepository.
     * Nguoi dung chua nop bai nao: tra ve danh sach rong.
     */
    @Test
    public void test04_FindByUserId_ChuaNopBai() {
        System.out.println("findByUserId - nguoi dung chua nop bai nao (tuanpv)");
        saveSubmission(hien, k001, Verdict.AC);

        List<Submission> result = instance.findByUserId(tuan.getId());

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ================================================================
    // 2. Xem chi tiet bai nop - findById()
    // ================================================================

    /**
     * Test of findById method, of class SubmissionRepository.
     * Ma bai nop ton tai: tra ve bai nop kem ket qua tung test.
     */
    @Test
    public void test05_FindById_TonTai() {
        System.out.println("findById - ma bai nop ton tai");
        Submission saved = saveSubmission(hien, k001, Verdict.AC);

        Submission result = instance.findById(saved.getId());

        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
        assertEquals(hien.getId(), result.getAuthor().getId());
        assertEquals("K001", result.getProblem().getId());
        assertEquals("C++", result.getLanguage().getName());
        assertTrue(result.isJudged());
        assertEquals(Verdict.AC, result.getResult().getOverallVerdict());
        assertEquals(100.0, result.getResult().getScore(), 0.001);

        List<TestCaseResult> tests = result.getResult().getTestResults();
        assertEquals(8, tests.size());
        assertEquals("sample01", tests.get(0).getTestCase().getId());
        assertEquals("07", tests.get(7).getTestCase().getId());
        for (TestCaseResult t : tests) {
            assertEquals(Verdict.AC, t.getVerdict());
            assertEquals(12.5, t.getEarnedPoints(), 0.001);
        }
    }

    /**
     * Test of findById method, of class SubmissionRepository.
     * Ma bai nop khong ton tai: tra ve null.
     */
    @Test
    public void test06_FindById_KhongTonTai() {
        System.out.println("findById - ma bai nop khong ton tai");
        String id = "SUB-KHONG-TON-TAI";
        Submission result = instance.findById(id);
        assertNull(result);
    }

    // ================================================================
    // 3. Xem bang xep hang - solvedCountByUser(), findAll()
    // ================================================================

    /**
     * Test of solvedCountByUser method, of class UserRepository.
     * Mot nguoi dat AC hai lan cung mot bai: chi tinh mot bai.
     */
    @Test
    public void test07_SolvedCountByUser_HaiLanACCungMotBai() {
        System.out.println("solvedCountByUser - dat AC hai lan cung mot bai (hiennm, K001)");
        saveSubmission(hien, k001, Verdict.AC);
        saveSubmission(hien, k001, Verdict.AC);

        Map<String, Integer> result = users.solvedCountByUser();

        assertEquals(Integer.valueOf(1), result.get(hien.getId()));
    }

    /**
     * Test of solvedCountByUser method, of class UserRepository.
     * Nguoi chua co bai nao dat AC: so bai da giai bang 0.
     */
    @Test
    public void test08_SolvedCountByUser_ChuaCoBaiAC() {
        System.out.println("solvedCountByUser - chua co bai nao dat AC (lananh, tuanpv)");
        saveSubmission(lan, k001, Verdict.WA);

        Map<String, Integer> result = users.solvedCountByUser();

        assertEquals(Integer.valueOf(0), result.get(lan.getId()));     // co nop nhung chi WA
        assertEquals(Integer.valueOf(0), result.get(tuan.getId()));    // chua nop bai nao
    }

    /**
     * Test of findAll method, of class SubmissionRepository.
     * Chua co bai nop nao: tra ve danh sach rong.
     */
    @Test
    public void test09_FindAll_ChuaCoBaiNop() {
        System.out.println("findAll - chua co bai nop nao");
        List<Submission> result = instance.findAll();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * Test of findAll method, of class SubmissionRepository.
     * Co nhieu bai nop: tra ve du moi bai.
     */
    @Test
    public void test10_FindAll_NhieuBaiNop() {
        System.out.println("findAll - co nhieu bai nop");
        Submission a = saveSubmission(hien, k001, Verdict.AC);
        Submission b = saveSubmission(lan, k001, Verdict.WA);
        Submission c = saveSubmission(lan, k002, Verdict.AC);
        Submission d = saveSubmission(tuan, k001, Verdict.AC);

        List<Submission> result = instance.findAll();

        assertEquals(4, result.size());
        List<String> ids = new ArrayList<>();
        for (Submission s : result) {
            ids.add(s.getId());
        }
        assertTrue(ids.contains(a.getId()));
        assertTrue(ids.contains(b.getId()));
        assertTrue(ids.contains(c.getId()));
        assertTrue(ids.contains(d.getId()));
    }

    // ================================================================
    // Du lieu mau
    // ================================================================

    /** Tao mot bai tap co soLuongTest test, test dau la test vi du, tong diem 100. */
    private Problem createProblem(String id, String title, int soLuongTest) {
        Problem problem = new Problem(id, title, "", 1000, 128, "token");
        double points = 100.0 / soLuongTest;
        problem.addTestCase(new TestCase("sample01", "5", "5", points, true));
        for (int i = 1; i < soLuongTest; i++) {
            problem.addTestCase(new TestCase("0" + i, String.valueOf(i), String.valueOf(i), points, false));
        }
        problems.save(problem);
        return problem;
    }

    /** Tao mot tai khoan sinh vien rieng cho test, ten dang nhap khong trung voi tai khoan that. */
    private User createStudent(String fullName, String studentCode) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        Student student = new Student("U-TEST-" + suffix.toUpperCase(Locale.ROOT), "t4_" + suffix,
                fullName, studentCode, "D24CQCN01");
        // Thong tin xac thuc gia: test nay khong dang nhap nen khong can bam mat khau that.
        users.saveWithCredentials(student, new Credentials("aGFzaA==", "c2FsdA==", 1));
        createdUserIds.add(student.getId());
        return student;
    }

    /**
     * Tao mot bai nop da cham roi luu vao kho. Verdict AC thi moi test deu AC va du diem,
     * verdict khac thi moi test deu nhan verdict do va 0 diem.
     */
    private Submission saveSubmission(User author, Problem problem, Verdict verdict) {
        submissionCount++;
        String id = "SUB-TEST-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
        Submission submission = new Submission(id, problem, author, cpp, "solution.cpp",
                BASE_TIME.plusSeconds(submissionCount));
        submission.setSourceCode("#include <iostream>\nint main() { long long n; std::cin >> n; std::cout << n; }\n");

        JudgeResult result = new JudgeResult(submission);
        for (TestCase test : problem.getTestCases()) {
            if (verdict == Verdict.AC) {
                result.addTestResult(new TestCaseResult(test, Verdict.AC, 8, test.getPoints(), ""));
            } else {
                result.addTestResult(new TestCaseResult(test, verdict, 8, 0, "Lệch tại giá trị thứ 1"));
            }
        }
        result.setJudgeTimeMs(120);
        submission.setResult(result);

        instance.saveWithResults(submission);
        createdSubmissionIds.add(submission.getId());
        return submission;
    }
}
