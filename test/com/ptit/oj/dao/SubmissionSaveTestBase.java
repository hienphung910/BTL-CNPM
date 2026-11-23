/*
 * Kiem thu don vi (JUnit 5) cho tang DAO cua Module 3 - Nop bai va cham tu dong.
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
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Cac test dung chung cho moi cach luu bai nop (bo nho hay MySQL): tim bai tap theo ma,
 * luu bai nop cung ket qua cham, doc lai bai vua nop.
 * Lop con chi can tao hai kho du lieu: kho bai nop va kho tai khoan.
 *
 * Truong hop "loi khi dang ghi ket qua test" (test06) can transaction that cua CSDL
 * nen chi nam trong lop MySqlSubmissionSaveTest.
 *
 * Moi test tu tao tai khoan va bai nop cua rieng minh, xong thi xoa o tearDown(),
 * nen cac test khong anh huong lan nhau va khong dung toi du lieu da co.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class SubmissionSaveTestBase {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 11, 17, 8, 54, 30);

    /** Thong bao cua trinh bien dich, co nhieu dong de kiem tra viec luu nguyen van. */
    private static final String COMPILE_MESSAGE =
            "solution.cpp: In function 'int main()':\n"
            + "solution.cpp:3:28: error: 'm' was not declared in this scope\n"
            + "    3 |     long long n; std::cin >> m;\n"
            + "      |                            ^\n";

    /** Kho bai nop can kiem thu. */
    protected SubmissionRepository instance;

    /** Kho tai khoan di cung: bai nop nao cung phai co nguoi nop. */
    protected UserRepository users;

    /** Kho bai tap va danh sach ngon ngu: bai tap khong nam trong CSDL nen luon o trong bo nho. */
    protected Repository<Problem> problems;
    protected LanguageRegistry languages;

    protected Problem k001;
    protected Language cpp;
    protected User student;

    /** Ma bai nop va ma tai khoan do test tao ra, xoa o tearDown(). */
    protected List<String> createdSubmissionIds = new ArrayList<>();
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
        cpp = languages.byName("C++");

        createRepositories();

        student = createStudent("Nguyễn Tiến Khôi", "B24DCCN325");
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
    // 1. Tim bai tap theo ma - Repository.findById()
    // ================================================================

    /**
     * Test of findById method, of class Repository (kho bai tap).
     * Ma bai ton tai: tra ve bai tap kem du bo test, test vi du dung truoc test an.
     */
    @Test
    public void test01_FindProblemById_MaBaiTonTai() {
        System.out.println("findById (kho bai tap) - ma bai ton tai");

        Problem result = problems.findById("K001");

        assertNotNull(result);
        assertEquals("K001", result.getId());
        assertEquals("Con vẹt Lóc Cóc nhại số", result.getTitle());
        List<TestCase> tests = result.getTestCases();
        assertEquals(8, tests.size());
        assertTrue(tests.get(0).isSample());
        for (int i = 1; i < tests.size(); i++) {
            assertFalse(tests.get(i).isSample());
        }
        assertEquals(100.0, result.getMaxPoints(), 0.001);
    }

    /**
     * Test of findById method, of class Repository (kho bai tap).
     * Ma bai khong ton tai: tra ve null.
     */
    @Test
    public void test02_FindProblemById_MaBaiKhongTonTai() {
        System.out.println("findById (kho bai tap) - ma bai khong ton tai");
        assertNull(problems.findById("K999"));
    }

    // ================================================================
    // 2. Luu bai nop cung ket qua cham - saveWithResults()
    // ================================================================

    /**
     * Test of saveWithResults method, of class SubmissionRepository.
     * Bai dat AC voi 8 test: them mot bai nop va 8 ket qua test.
     */
    @Test
    public void test03_SaveWithResults_BaiDatAC() {
        System.out.println("saveWithResults - bai dat AC voi 8 test");
        int before = instance.count();
        Submission submission = createSubmission();
        submission.setResult(createResult(submission, Verdict.AC));

        save(submission);

        assertEquals(before + 1, instance.count());
        Submission saved = instance.findById(submission.getId());
        assertNotNull(saved);
        assertEquals(Verdict.AC, saved.getResult().getOverallVerdict());
        assertEquals(8, saved.getResult().getTestResults().size());
        assertEquals(8, saved.getResult().getPassedCount());
        assertEquals(100.0, saved.getResult().getScore(), 0.001);
    }

    /**
     * Test of saveWithResults method, of class SubmissionRepository.
     * Bai loi bien dich: them mot bai nop voi verdict CE, khong co ket qua test nao.
     */
    @Test
    public void test04_SaveWithResults_BaiLoiBienDich() {
        System.out.println("saveWithResults - bai loi bien dich");
        int before = instance.count();
        Submission submission = createSubmission();
        JudgeResult result = new JudgeResult(submission);
        result.markGlobal(Verdict.CE, COMPILE_MESSAGE);
        submission.setResult(result);

        save(submission);

        assertEquals(before + 1, instance.count());
        Submission saved = instance.findById(submission.getId());
        assertNotNull(saved);
        assertEquals(Verdict.CE, saved.getResult().getOverallVerdict());
        assertEquals(0, saved.getResult().getTestResults().size());
        assertEquals(0.0, saved.getResult().getScore(), 0.001);
    }

    /**
     * Test of saveWithResults method, of class SubmissionRepository.
     * Luu lai mot bai nop da co ma: ghi de dong cu, khong sinh them dong moi.
     */
    @Test
    public void test05_SaveWithResults_LuuLaiBaiDaCoMa() {
        System.out.println("saveWithResults - luu lai bai nop da co ma");
        Submission submission = createSubmission();
        submission.setResult(createResult(submission, Verdict.WA));
        save(submission);
        int before = instance.count();

        // Cham lai chinh bai nop do, lan nay dat AC.
        submission.setResult(createResult(submission, Verdict.AC));
        save(submission);

        assertEquals(before, instance.count());
        Submission saved = instance.findById(submission.getId());
        assertEquals(Verdict.AC, saved.getResult().getOverallVerdict());
        assertEquals(8, saved.getResult().getTestResults().size());   // van 8 dong, khong thanh 16
        assertEquals(100.0, saved.getResult().getScore(), 0.001);
    }

    // test06 (loi khi dang ghi ket qua test) nam trong MySqlSubmissionSaveTest.

    // ================================================================
    // 3. Doc lai bai vua nop - findById()
    // ================================================================

    /**
     * Test of findById method, of class SubmissionRepository.
     * Bai vua luu: doc lai dung verdict, diem va ket qua tung test theo dung thu tu cham.
     */
    @Test
    public void test07_FindById_BaiVuaLuu() {
        System.out.println("findById - doc lai bai vua luu");
        Submission submission = createSubmission();
        // 8 test: 6 test AC, test thu 6 sai dap an, test thu 7 qua thoi gian.
        JudgeResult result = new JudgeResult(submission);
        List<TestCase> tests = k001.getTestCases();
        for (int i = 0; i < tests.size(); i++) {
            TestCase test = tests.get(i);
            if (i == 5) {
                result.addTestResult(new TestCaseResult(test, Verdict.WA, 9, 0, "Lệch tại giá trị thứ 1"));
            } else if (i == 6) {
                result.addTestResult(new TestCaseResult(test, Verdict.TLE, 1000, 0, "Quá 1000 ms"));
            } else {
                result.addTestResult(new TestCaseResult(test, Verdict.AC, 8 + i, test.getPoints(), ""));
            }
        }
        result.setJudgeTimeMs(1350);
        submission.setResult(result);
        save(submission);

        Submission saved = instance.findById(submission.getId());

        assertNotNull(saved);
        assertEquals(submission.getId(), saved.getId());
        assertEquals("K001", saved.getProblem().getId());
        assertEquals(student.getId(), saved.getAuthor().getId());
        assertEquals("C++", saved.getLanguage().getName());
        assertEquals(submission.getSourceCode(), saved.getSourceCode());
        assertEquals(submission.getSubmittedAt(), saved.getSubmittedAt());

        JudgeResult savedResult = saved.getResult();
        assertEquals(Verdict.TLE, savedResult.getOverallVerdict());     // loi nang nhat trong cac test
        assertEquals(75.0, savedResult.getScore(), 0.001);              // 6 test dung x 12.5 diem
        assertEquals(6, savedResult.getPassedCount());
        assertEquals(1350, savedResult.getJudgeTimeMs());

        List<TestCaseResult> savedTests = savedResult.getTestResults();
        assertEquals(8, savedTests.size());
        for (int i = 0; i < tests.size(); i++) {
            TestCaseResult expected = result.getTestResults().get(i);
            TestCaseResult actual = savedTests.get(i);
            assertEquals(tests.get(i).getId(), actual.getTestCase().getId());   // dung thu tu cham
            assertEquals(expected.getVerdict(), actual.getVerdict());
            assertEquals(expected.getRuntimeMs(), actual.getRuntimeMs());
            assertEquals(expected.getEarnedPoints(), actual.getEarnedPoints(), 0.001);
            assertEquals(expected.getMessage(), actual.getMessage());
        }
    }

    /**
     * Test of findById method, of class SubmissionRepository.
     * Bai loi bien dich: doc lai verdict CE kem nguyen van thong bao cua trinh bien dich.
     */
    @Test
    public void test08_FindById_BaiLoiBienDich() {
        System.out.println("findById - doc lai bai loi bien dich");
        Submission submission = createSubmission();
        JudgeResult result = new JudgeResult(submission);
        result.markGlobal(Verdict.CE, COMPILE_MESSAGE);
        submission.setResult(result);
        save(submission);

        Submission saved = instance.findById(submission.getId());

        assertNotNull(saved);
        assertEquals(Verdict.CE, saved.getResult().getOverallVerdict());
        assertEquals(COMPILE_MESSAGE, saved.getResult().getGlobalMessage());
        assertEquals(0, saved.getResult().getPassedCount());
        assertEquals(8, saved.getResult().getTotalTests());     // de bai van co 8 test, chi la chua chay
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
        Student newStudent = new Student("U-TEST-" + suffix.toUpperCase(Locale.ROOT), "t3_" + suffix,
                fullName, studentCode, "D24CQCN01");
        // Thong tin xac thuc gia: test nay khong dang nhap nen khong can bam mat khau that.
        users.saveWithCredentials(newStudent, new Credentials("aGFzaA==", "c2FsdA==", 1));
        createdUserIds.add(newStudent.getId());
        return newStudent;
    }

    /** Tao mot bai nop C++ cho bai K001, chua co ket qua cham va chua luu vao kho. */
    protected Submission createSubmission() {
        submissionCount++;
        String id = "SUB-TEST-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
        Submission submission = new Submission(id, k001, student, cpp, "solution.cpp",
                BASE_TIME.plusSeconds(submissionCount));
        submission.setSourceCode("#include <iostream>\nint main() { long long n; std::cin >> n; std::cout << n; }\n");
        return submission;
    }

    /**
     * Ket qua cham ma moi test nhan cung mot verdict: AC thi du diem, verdict khac thi 0 diem.
     */
    protected JudgeResult createResult(Submission submission, Verdict verdict) {
        JudgeResult result = new JudgeResult(submission);
        for (TestCase test : submission.getProblem().getTestCases()) {
            if (verdict == Verdict.AC) {
                result.addTestResult(new TestCaseResult(test, Verdict.AC, 8, test.getPoints(), ""));
            } else {
                result.addTestResult(new TestCaseResult(test, verdict, 8, 0, "Lệch tại giá trị thứ 1"));
            }
        }
        result.setJudgeTimeMs(120);
        return result;
    }

    /** Luu bai nop va ghi nho ma de tearDown() xoa di. */
    protected void save(Submission submission) {
        if (!createdSubmissionIds.contains(submission.getId())) {
            createdSubmissionIds.add(submission.getId());
        }
        instance.saveWithResults(submission);
    }
}
