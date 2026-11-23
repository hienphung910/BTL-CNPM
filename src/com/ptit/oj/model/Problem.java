package com.ptit.oj.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Problem extends Entity {
    private String title;
    private String statement;
    private long timeLimitMs;
    private int memoryLimitMb;
    private String comparatorSpec;

    private String topic;

    private int difficulty;

    private int order;
    private List<TestCase> testCases = new ArrayList<>();

    public Problem(String id, String title, String statement,
                   long timeLimitMs, int memoryLimitMb, String comparatorSpec) {
        this(id, title, statement, timeLimitMs, memoryLimitMb, comparatorSpec, "", 0, 0);
    }

    public Problem(String id, String title, String statement,
                   long timeLimitMs, int memoryLimitMb, String comparatorSpec,
                   String topic, int difficulty, int order) {
        super(id);
        this.title = title;
        this.statement = statement == null ? "" : statement;
        this.timeLimitMs = timeLimitMs;
        this.memoryLimitMb = memoryLimitMb;
        this.comparatorSpec = comparatorSpec == null ? "token" : comparatorSpec;
        this.topic = topic == null ? "" : topic;
        this.difficulty = difficulty;
        this.order = order;
    }

    public void addTestCase(TestCase tc) {
        testCases.add(tc);
    }

    public List<TestCase> getTestCases() {
        return Collections.unmodifiableList(testCases);
    }

    public String getTitle() { return title; }
    public String getStatement() { return statement; }
    public long getTimeLimitMs() { return timeLimitMs; }
    public int getMemoryLimitMb() { return memoryLimitMb; }
    public String getComparatorSpec() { return comparatorSpec; }
    public String getTopic() { return topic; }
    public int getDifficulty() { return difficulty; }
    public int getOrder() { return order; }

    public double getMaxPoints() {
        double sum = 0;
        for (TestCase tc : testCases) sum += tc.getPoints();
        return sum;
    }
}
