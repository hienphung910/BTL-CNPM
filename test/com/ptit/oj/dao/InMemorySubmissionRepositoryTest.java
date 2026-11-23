package com.ptit.oj.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class InMemorySubmissionRepositoryTest extends SubmissionRepositoryTestBase {
    public InMemorySubmissionRepositoryTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        System.out.println("=== Kiem thu InMemorySubmissionRepository ===");
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @Override
    protected void createRepositories() {
        InMemorySubmissionRepository submissionRepository = new InMemorySubmissionRepository();
        InMemoryUserRepository userRepository = new InMemoryUserRepository();

        userRepository.setSubmissionRepository(submissionRepository);

        instance = submissionRepository;
        users = userRepository;
    }
}
