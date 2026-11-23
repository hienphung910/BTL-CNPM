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

@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class SubmissionRepositoryTestBase {
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 11, 17, 8, 54, 30);

    protected SubmissionRepository instance;

    protected UserRepository users;

    protected Repository<Problem> problems;
    protected LanguageRegistry languages;

    private Problem k001;
    private Problem k002;
    private Language cpp;

    private User hien;
    private User lan;
    private User tuan;

    private List<String> createdSubmissionIds = new ArrayList<>();
    private List<String> createdUserIds = new ArrayList<>();

    private int submissionCount = 0;

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

    @Test
    public void test01_FindAllNewestFirst_ChuaCoBaiNop() {
        System.out.println("findAllNewestFirst - chua co bai nop nao");
        List<Submission> result = instance.findAllNewestFirst();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

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

    @Test
    public void test04_FindByUserId_ChuaNopBai() {
        System.out.println("findByUserId - nguoi dung chua nop bai nao (tuanpv)");
        saveSubmission(hien, k001, Verdict.AC);

        List<Submission> result = instance.findByUserId(tuan.getId());

        assertNotNull(result);
        assertEquals(0, result.size());
    }

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

    @Test
    public void test06_FindById_KhongTonTai() {
        System.out.println("findById - ma bai nop khong ton tai");
        String id = "SUB-KHONG-TON-TAI";
        Submission result = instance.findById(id);
        assertNull(result);
    }

    @Test
    public void test07_SolvedCountByUser_HaiLanACCungMotBai() {
        System.out.println("solvedCountByUser - dat AC hai lan cung mot bai (hiennm, K001)");
        saveSubmission(hien, k001, Verdict.AC);
        saveSubmission(hien, k001, Verdict.AC);

        Map<String, Integer> result = users.solvedCountByUser();

        assertEquals(Integer.valueOf(1), result.get(hien.getId()));
    }

    @Test
    public void test08_SolvedCountByUser_ChuaCoBaiAC() {
        System.out.println("solvedCountByUser - chua co bai nao dat AC (lananh, tuanpv)");
        saveSubmission(lan, k001, Verdict.WA);

        Map<String, Integer> result = users.solvedCountByUser();

        assertEquals(Integer.valueOf(0), result.get(lan.getId()));
        assertEquals(Integer.valueOf(0), result.get(tuan.getId()));
    }

    @Test
    public void test09_FindAll_ChuaCoBaiNop() {
        System.out.println("findAll - chua co bai nop nao");
        List<Submission> result = instance.findAll();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

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

    private User createStudent(String fullName, String studentCode) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        Student student = new Student("U-TEST-" + suffix.toUpperCase(Locale.ROOT), "t4_" + suffix,
                fullName, studentCode, "D24CQCN01");

        users.saveWithCredentials(student, new Credentials("aGFzaA==", "c2FsdA==", 1));
        createdUserIds.add(student.getId());
        return student;
    }

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
