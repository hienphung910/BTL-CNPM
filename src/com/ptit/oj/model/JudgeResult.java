package com.ptit.oj.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tổng hợp kết quả của một bài nộp: verdict chung, điểm, số test đúng và thời gian chấm.
 */
public class JudgeResult {

    // Bài nộp, kết quả từng test, lỗi chung và tổng thời gian chấm.
    private Submission submission;
    private List<TestCaseResult> testResults = new ArrayList<>();
    private Verdict globalVerdict;      // CE/IE: lỗi chung, không chạy test nào.
    private String globalMessage = "";
    private long judgeTimeMs;

    /**
     * Liên kết đối tượng tổng hợp kết quả với bài nộp cần chấm.
     */
    public JudgeResult(Submission submission) {
        this.submission = submission;
    }

    /**
     * Thêm kết quả của một test vào danh sách tổng hợp.
     */
    public void addTestResult(TestCaseResult r) {
        testResults.add(r);
    }

    /**
     * Ghi lỗi chung của cả bài như CE hoặc IE, ưu tiên hơn kết quả từng test.
     */
    public void markGlobal(Verdict verdict, String message) {
        this.globalVerdict = verdict;
        this.globalMessage = message == null ? "" : message;
    }

    /**
     * Trả góc nhìn chỉ đọc của danh sách kết quả test, tránh sửa trực tiếp từ bên ngoài.
     */
    public List<TestCaseResult> getTestResults() {
        return Collections.unmodifiableList(testResults);
    }

    /**
     * Ưu tiên lỗi chung; nếu chưa có kết quả trả IE, còn lại chọn verdict có severity lớn nhất.
     */
    public Verdict getOverallVerdict() {
        if (globalVerdict != null) return globalVerdict;
        if (testResults.isEmpty()) return Verdict.IE;
        Verdict worst = Verdict.AC;
        for (TestCaseResult r : testResults) {
            if (r.getVerdict().getSeverity() > worst.getSeverity()) worst = r.getVerdict();
        }
        return worst;
    }

    /**
     * Đếm số test có verdict AC.
     */
    public int getPassedCount() {
        int c = 0;
        for (TestCaseResult r : testResults) if (r.getVerdict().isAccepted()) c++;
        return c;
    }

    /**
     * Lấy tổng số test được định nghĩa trong bài tập.
     */
    public int getTotalTests() {
        return submission.getProblem().getTestCases().size();
    }

    /**
     * Cộng điểm thực nhận của toàn bộ kết quả test.
     */
    public double getScore() {
        double s = 0;
        for (TestCaseResult r : testResults) s += r.getEarnedPoints();
        return s;
    }

    /**
     * Lấy thời gian chạy lớn nhất trong các test, hoặc 0 nếu chưa có test.
     */
    public long getMaxRuntimeMs() {
        long m = 0;
        for (TestCaseResult r : testResults) m = Math.max(m, r.getRuntimeMs());
        return m;
    }

    /**
     * Trả bài nộp tương ứng.
     */
    public Submission getSubmission() { return submission; }

    /**
     * Trả thông báo chung của cả bài.
     */
    public String getGlobalMessage() { return globalMessage; }

    /**
     * Khôi phục thông báo chung, chuyển null thành rỗng và không thay đổi globalVerdict.
     */
    public void setGlobalMessage(String globalMessage) {
        this.globalMessage = globalMessage == null ? "" : globalMessage;
    }

    /**
     * Trả tổng thời gian chấm, tính bằng mili giây.
     */
    public long getJudgeTimeMs() { return judgeTimeMs; }

    /**
     * Cập nhật tổng thời gian chấm, tính bằng mili giây.
     */
    public void setJudgeTimeMs(long judgeTimeMs) { this.judgeTimeMs = judgeTimeMs; }
}
