package com.ptit.oj.model;

public class TestCaseResult {
    private TestCase testCase;
    private Verdict verdict;
    private long runtimeMs;
    private double earnedPoints;
    private String message;

    public TestCaseResult(TestCase testCase, Verdict verdict, long runtimeMs,
                          double earnedPoints, String message) {
        this.testCase = testCase;
        this.verdict = verdict;
        this.runtimeMs = runtimeMs;
        this.earnedPoints = earnedPoints;
        this.message = message == null ? "" : message;
    }

    public TestCase getTestCase() { return testCase; }

    public Verdict getVerdict() { return verdict; }

    public long getRuntimeMs() { return runtimeMs; }

    public double getEarnedPoints() { return earnedPoints; }

    public String getMessage() { return message; }
}
