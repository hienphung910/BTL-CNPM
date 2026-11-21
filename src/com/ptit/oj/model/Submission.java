package com.ptit.oj.model;

import com.ptit.oj.controller.judge.Language;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

/**
 * Biểu diễn một lần nộp bài: bài tập, người nộp, ngôn ngữ, mã nguồn và thời điểm nộp.
 * Kết quả được gắn vào bài nộp sau khi chấm xong.
 */
public class Submission extends Entity {

    // Thông tin định danh, tác giả, mã nguồn và kết quả của lần nộp bài.
    private Problem problem;
    private User author;
    private Language language;
    private String fileName;        // Tên file mã nguồn, ví dụ Main.java.
    private LocalDateTime submittedAt;
    private String sourceCode;
    private JudgeResult result;           // Được gắn sau khi chấm.

    /**
     * Tạo bài nộp mới: sinh UUID, lấy thời điểm hiện tại và lưu nội dung mã nguồn.
     */
    public Submission(Problem problem, User author, Language language, String fileName, String sourceCode) {
        this(newId(), problem, author, language, fileName, LocalDateTime.now());
        this.sourceCode = sourceCode;
    }

    /**
     * Khôi phục thông tin bài nộp từ dữ liệu đã lưu; mã nguồn và kết quả được gắn riêng.
     */
    public Submission(String id, Problem problem, User author, Language language,
                      String fileName, LocalDateTime submittedAt) {
        super(id);
        this.problem = problem;
        this.author = author;
        this.language = language;
        this.fileName = fileName;
        this.submittedAt = submittedAt;
    }

    /**
     * Sinh mã bài nộp dạng SUB- kết hợp UUID viết hoa để giảm khả năng trùng mã.
     */
    public static String newId() {
        return "SUB-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
    }

    /**
     * Trả bài tập của lần nộp.
     */
    public Problem getProblem() { return problem; }

    /**
     * Trả người nộp bài.
     */
    public User getAuthor() { return author; }

    /**
     * Trả ngôn ngữ của bài nộp.
     */
    public Language getLanguage() { return language; }

    /**
     * Trả tên file mã nguồn.
     */
    public String getFileName() { return fileName; }

    /**
     * Trả thời điểm nộp bài.
     */
    public LocalDateTime getSubmittedAt() { return submittedAt; }

    /**
     * Trả nội dung mã nguồn.
     */
    public String getSourceCode() { return sourceCode; }

    /**
     * Cập nhật nội dung mã nguồn.
     */
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    /**
     * Trả kết quả chấm được gắn vào bài nộp.
     */
    public JudgeResult getResult() { return result; }

    /**
     * Cập nhật kết quả chấm được gắn vào bài nộp.
     */
    public void setResult(JudgeResult result) { this.result = result; }

    /**
     * Cho biết bài nộp đã được gắn một đối tượng kết quả hay chưa.
     */
    public boolean isJudged() { return result != null; }
}
