/*
 * Kiem thu don vi (JUnit 5) cho viec luu bai nop tren InMemorySubmissionRepository - Module 3.
 */
package com.ptit.oj.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * Chay cac test cua bang 6.1 tren kho du lieu trong bo nho (che do --no-db).
 * Moi test dung mot kho moi nen cac test khong anh huong lan nhau.
 *
 * Kho trong bo nho khong co transaction nen truong hop "loi khi dang ghi ket qua test"
 * (test06) chi chay tren MySQL, xem MySqlSubmissionSaveTest.
 */
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
