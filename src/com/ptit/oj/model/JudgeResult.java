package com.ptit.oj.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JudgeResult {
    private Submission submission;
    private List<TestCaseResult> testResults = new ArrayList<>();
    private Verdict globalVerdict;
    private String globalMessage = "";
    private long judgeTimeMs;

    public JudgeResult(Submission submission) {
        this.submission = submission;
    }

    public void addTestResult(TestCaseResult r) {
        testResults.add(r);
    }

    public void markGlobal(Verdict verdict, String message) {
        this.globalVerdict = verdict;
        this.globalMessage = message == null ? "" : message;
    }

    public List<TestCaseResult> getTestResults() {
        return Collections.unmodifiableList(testResults);
    }

    public Verdict getOverallVerdict() {
        if (globalVerdict != null) return globalVerdict;
        if (testResults.isEmpty()) return Verdict.IE;
        Verdict worst = Verdict.AC;
        for (TestCaseResult r : testResults) {
            if (r.getVerdict().getSeverity() > worst.getSeverity()) worst = r.getVerdict();
        }
        return worst;
    }

    public int getPassedCount() {
        int c = 0;
        for (TestCaseResult r : testResults) if (r.getVerdict().isAccepted()) c++;
        return c;
    }

    public int getTotalTests() {
        return submission.getProblem().getTestCases().size();
    }

    public double getScore() {
        double s = 0;
        for (TestCaseResult r : testResults) s += r.getEarnedPoints();
        return s;
    }

    public long getMaxRuntimeMs() {
        long m = 0;
        for (TestCaseResult r : testResults) m = Math.max(m, r.getRuntimeMs());
        return m;
    }

    public Submission getSubmission() { return submission; }

    public String getGlobalMessage() { return globalMessage; }

    public void setGlobalMessage(String globalMessage) {
        this.globalMessage = globalMessage == null ? "" : globalMessage;
    }

    public long getJudgeTimeMs() { return judgeTimeMs; }

    public void setJudgeTimeMs(long judgeTimeMs) { this.judgeTimeMs = judgeTimeMs; }
}
