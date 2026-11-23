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

public class Judge {
    private ProcessRunner runner = new ProcessRunner();

    public JudgeResult judge(Submission submission) {
        Problem problem = submission.getProblem();
        Language language = submission.getLanguage();
        JudgeResult result = new JudgeResult(submission);
        long startedAt = System.currentTimeMillis();

        Path workDir = null;
        try {
            if (!language.isAvailable()) {
                result.markGlobal(Verdict.IE, "Máy này chưa cài toolchain cho " + language.getName());
                return result;
            }
            if (problem.getTestCases().isEmpty()) {
                result.markGlobal(Verdict.IE, "Bài " + problem.getId() + " chưa có test nào");
                return result;
            }

            workDir = Files.createTempDirectory("oj-judge-");
            Path source = workDir.resolve(submission.getFileName());
            Files.write(source, submission.getSourceCode().getBytes(StandardCharsets.UTF_8));

            CompileResult compile = language.compile(source, workDir);
            if (!compile.isSuccess()) {
                result.markGlobal(Verdict.CE, compile.getMessage());
                return result;
            }

            OutputComparator comparator = ComparatorFactory.create(problem.getComparatorSpec());
            for (TestCase test : problem.getTestCases()) {
                result.addTestResult(runTestCase(problem, language, source, workDir, test, comparator));
            }
        } catch (IOException e) {
            result.markGlobal(Verdict.IE, "Lỗi vào/ra khi chấm: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.markGlobal(Verdict.IE, "Quá trình chấm bị ngắt");
        } finally {
            deleteRecursively(workDir);
            result.setJudgeTimeMs(System.currentTimeMillis() - startedAt);
            submission.setResult(result);
        }
        return result;
    }

    private TestCaseResult runTestCase(Problem problem, Language language, Path source,
                                       Path workDir, TestCase test, OutputComparator comparator)
            throws IOException, InterruptedException {
        List<String> command = language.buildRunCommand(source, workDir, problem.getMemoryLimitMb());
        long limit = problem.getTimeLimitMs() + language.getStartupOverheadMs();
        ExecutionResult exec = runner.run(command, workDir, test.getInput(), limit);

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

        if (comparator.matches(test.getExpectedOutput(), exec.getStdout())) {
            return new TestCaseResult(test, Verdict.AC, exec.getElapsedMs(), test.getPoints(), "");
        }
        return new TestCaseResult(test, Verdict.WA, exec.getElapsedMs(), 0,
                comparator.explain(test.getExpectedOutput(), exec.getStdout()));
    }

    private boolean looksLikeOutOfMemory(String stderr) {
        if (stderr == null || stderr.isEmpty()) return false;
        String s = stderr.toLowerCase();
        return s.contains("outofmemoryerror")
                || s.contains("bad_alloc")
                || s.contains("memoryerror")
                || s.contains("out of memory")
                || s.contains("cannot allocate memory");
    }

    private String firstLine(String text) {
        if (text != null) {
            for (String line : text.split("\r?\n")) {
                if (!line.trim().isEmpty()) return line.trim();
            }
        }
        return "(không có thông báo)";
    }

    private void deleteRecursively(Path dir) {
        if (dir == null) return;
        deleteRecursively(dir.toFile());
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
