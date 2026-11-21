package com.ptit.oj.controller.judge;

import com.ptit.oj.model.JudgeResult;
import com.ptit.oj.model.Problem;
import com.ptit.oj.model.Submission;
import com.ptit.oj.model.TestCase;
import com.ptit.oj.model.TestCaseResult;
import com.ptit.oj.model.Verdict;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Điều phối toàn bộ quá trình chấm: tạo thư mục tạm, ghi mã nguồn, biên dịch,
 * chạy từng test, so sánh output, tổng hợp kết quả rồi dọn dẹp.
 * Dùng Language và OutputComparator để tách xử lý ngôn ngữ khỏi cách so sánh đáp án.
 */
public class Judge {

    // Công cụ thực thi chương trình bài nộp cho từng test.
    private ProcessRunner runner = new ProcessRunner();

    /**
     * Chấm toàn bộ bài nộp; luôn dọn thư mục tạm và gắn kết quả vào Submission trong finally.
     */
    public JudgeResult judge(Submission submission) {
        Problem problem = submission.getProblem();
        Language language = submission.getLanguage();
        JudgeResult result = new JudgeResult(submission);
        long startedAt = System.currentTimeMillis();

        Path workDir = null;
        try {
            // Bước 1: kiểm tra toolchain và bộ test trước khi tạo file, chạy chương trình.
            if (!language.isAvailable()) {
                result.markGlobal(Verdict.IE, "Máy này chưa cài toolchain cho " + language.getName());
                return result;
            }
            if (problem.getTestCases().isEmpty()) {
                result.markGlobal(Verdict.IE, "Bài " + problem.getId() + " chưa có test nào");
                return result;
            }

            // Bước 2: tạo thư mục riêng cho lần chấm và ghi mã nguồn bằng UTF-8.
            workDir = Files.createTempDirectory("oj-judge-");
            Path source = workDir.resolve(submission.getFileName());
            Files.write(source, submission.getSourceCode().getBytes(StandardCharsets.UTF_8));

            // Bước 3: biên dịch hoặc kiểm tra cú pháp; nếu thất bại thì kết thúc với CE.
            CompileResult compile = language.compile(source, workDir);
            if (!compile.isSuccess()) {
                result.markGlobal(Verdict.CE, compile.getMessage());
                return result;
            }

            // Bước 4: chọn cách so sánh và thu kết quả của từng test, kể cả khi test trước sai.
            OutputComparator comparator = ComparatorFactory.create(problem.getComparatorSpec());
            for (TestCase test : problem.getTestCases()) {
                result.addTestResult(runTestCase(problem, language, source, workDir, test, comparator));
            }
        } catch (IOException e) {
            result.markGlobal(Verdict.IE, "Lỗi vào/ra khi chấm: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // Khôi phục cờ ngắt của thread.
            result.markGlobal(Verdict.IE, "Quá trình chấm bị ngắt");
        } finally {
            // Bước 5: luôn dọn file tạm, ghi thời gian và gắn kết quả, kể cả khi đã return.
            deleteRecursively(workDir);
            result.setJudgeTimeMs(System.currentTimeMillis() - startedAt);
            submission.setResult(result);
        }
        return result;
    }

    /**
     * Chạy một test rồi phân loại theo thứ tự timeout, lỗi bộ nhớ, lỗi runtime và kết quả so sánh.
     * Test AC nhận đủ điểm của test; các kết quả còn lại nhận 0 điểm.
     */
    private TestCaseResult runTestCase(Problem problem, Language language, Path source,
                                       Path workDir, TestCase test, OutputComparator comparator)
            throws IOException, InterruptedException {
        // Chuẩn bị lệnh chạy và cộng phần thời gian bù khởi động riêng cho ngôn ngữ.
        List<String> command = language.buildRunCommand(source, workDir, problem.getMemoryLimitMb());
        long limit = problem.getTimeLimitMs() + language.getStartupOverheadMs();
        ExecutionResult exec = runner.run(command, workDir, test.getInput(), limit);

        // Phân loại lỗi thực thi trước khi xét output: TLE, MLE rồi RE.
        if (exec.isTimedOut()) {
            return new TestCaseResult(test, Verdict.TLE, exec.getElapsedMs(), 0,
                    "Chạy quá " + problem.getTimeLimitMs() + " ms");
        }
        if (looksLikeOutOfMemory(exec.getStderr())) {
            return new TestCaseResult(test, Verdict.MLE, exec.getElapsedMs(), 0,
                    "Vượt giới hạn " + problem.getMemoryLimitMb() + " MB");
        }
        if (exec.getExitCode() != 0) {
            return new TestCaseResult(test, Verdict.RE, exec.getElapsedMs(), 0,
                    "Exit code " + exec.getExitCode() + ": " + firstLine(exec.getStderr()));
        }
        // Chương trình chạy xong: output khớp nhận AC và điểm test; không khớp nhận WA.
        if (comparator.matches(test.getExpectedOutput(), exec.getStdout())) {
            return new TestCaseResult(test, Verdict.AC, exec.getElapsedMs(), test.getPoints(), "");
        }
        return new TestCaseResult(test, Verdict.WA, exec.getElapsedMs(), 0,
                comparator.explain(test.getExpectedOutput(), exec.getStdout()));
    }

    /**
     * Nhận diện lỗi bộ nhớ qua chuỗi thông báo trên stderr của runtime.
     * Đây là cách nhận diện gần đúng, không đo trực tiếp lượng bộ nhớ tiến trình.
     */
    private boolean looksLikeOutOfMemory(String stderr) {
        if (stderr == null || stderr.isEmpty()) return false;
        String s = stderr.toLowerCase();
        return s.contains("outofmemoryerror")            // Java
                || s.contains("bad_alloc")               // C++
                || s.contains("memoryerror")             // Python
                || s.contains("out of memory")           // JavaScript (Node) và các runtime khác.
                || s.contains("cannot allocate memory");
    }

    /**
     * Lấy dòng lỗi đầu tiên không rỗng để rút gọn thông báo runtime.
     */
    private String firstLine(String text) {
        if (text != null) {
            for (String line : text.split("\r?\n")) {
                if (!line.trim().isEmpty()) return line.trim();
            }
        }
        return "(không có thông báo)";
    }

    /**
     * Xóa nội dung trong thư mục tạm trước rồi xóa chính thư mục hoặc file.
     * Việc xóa có thể thất bại nếu file còn bị hệ điều hành khóa.
     */
    private void deleteRecursively(Path dir) {
        if (dir == null) return;
        deleteRecursively(dir.toFile());
    }

    /**
     * Xóa nội dung trong thư mục tạm trước rồi xóa chính thư mục hoặc file.
     * Việc xóa có thể thất bại nếu file còn bị hệ điều hành khóa.
     */
    private void deleteRecursively(File file) {
        File[] children = file.listFiles();         // Trả null nếu đây là file thường.
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        // File .exe còn bị khóa trên Windows có thể không xóa được tại thời điểm này.
        file.delete();
    }
}
