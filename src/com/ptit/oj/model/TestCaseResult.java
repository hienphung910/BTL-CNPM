package com.ptit.oj.model;

/**
 * Lưu kết quả của một test: verdict, thời gian chạy, điểm và thông báo.
 * Thông báo dành cho người nộp không được đưa đáp án của test ẩn vào nội dung.
 */
public class TestCaseResult {

    // Thông tin test và các giá trị kết quả dùng để tính điểm, hiển thị và lưu trữ.
    private TestCase testCase;
    private Verdict verdict;
    private long runtimeMs;
    private double earnedPoints;
    private String message;

    /**
     * Ghi nhận test, verdict, thời gian, điểm và thông báo; chuyển thông báo null thành rỗng.
     */
    public TestCaseResult(TestCase testCase, Verdict verdict, long runtimeMs,
                          double earnedPoints, String message) {
        this.testCase = testCase;
        this.verdict = verdict;
        this.runtimeMs = runtimeMs;
        this.earnedPoints = earnedPoints;
        this.message = message == null ? "" : message;
    }

    /**
     * Trả test tương ứng.
     */
    public TestCase getTestCase() { return testCase; }

    /**
     * Trả trạng thái kết quả của test.
     */
    public Verdict getVerdict() { return verdict; }

    /**
     * Trả thời gian chạy test, tính bằng mili giây.
     */
    public long getRuntimeMs() { return runtimeMs; }

    /**
     * Trả điểm đạt được của test.
     */
    public double getEarnedPoints() { return earnedPoints; }

    /**
     * Trả thông báo kết quả.
     */
    public String getMessage() { return message; }
}
