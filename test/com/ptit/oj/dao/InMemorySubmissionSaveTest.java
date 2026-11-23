package com.ptit.oj.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class InMemorySubmissionSaveTest extends SubmissionSaveTestBase {
    public InMemorySubmissionSaveTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        System.out.println("=== Kiem thu luu bai nop tren InMemorySubmissionRepository ===");
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @Override
    protected void createRepositories() {
        instance = new InMemorySubmissionRepository();
        users = new InMemoryUserRepository();
    }
}
