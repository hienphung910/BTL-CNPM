/*
 * Kiem thu don vi (JUnit 5) cho InMemorySubmissionRepository - Module 4.
 */
package com.ptit.oj.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * Chay 10 test cua bang 6.1 tren kho du lieu trong bo nho (che do --no-db).
 * Moi test dung mot kho moi nen cac test khong anh huong lan nhau.
 *
 * @author Thai Duy Manh
 */
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
        // Kho tai khoan can kho bai nop de dem so bai da giai.
        userRepository.setSubmissionRepository(submissionRepository);

        instance = submissionRepository;
        users = userRepository;
    }
}
