package com.ptit.oj.model;

import com.ptit.oj.controller.judge.Language;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

public class Submission extends Entity {
    private Problem problem;
    private User author;
    private Language language;
    private String fileName;
    private LocalDateTime submittedAt;
    private String sourceCode;
    private JudgeResult result;

    public Submission(Problem problem, User author, Language language, String fileName, String sourceCode) {
        this(newId(), problem, author, language, fileName, LocalDateTime.now());
        this.sourceCode = sourceCode;
    }

    public Submission(String id, Problem problem, User author, Language language,
                      String fileName, LocalDateTime submittedAt) {
        super(id);
        this.problem = problem;
        this.author = author;
        this.language = language;
        this.fileName = fileName;
        this.submittedAt = submittedAt;
    }

    public static String newId() {
        return "SUB-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
    }

    public Problem getProblem() { return problem; }

    public User getAuthor() { return author; }

    public Language getLanguage() { return language; }

    public String getFileName() { return fileName; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }

    public String getSourceCode() { return sourceCode; }

    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public JudgeResult getResult() { return result; }

    public void setResult(JudgeResult result) { this.result = result; }

    public boolean isJudged() { return result != null; }
}
