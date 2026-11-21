/*
 * Kiem thu don vi (JUnit 5) cho InMemoryUserRepository - Module 1.
 */
package com.ptit.oj.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * Chay 12 test cua bang 6.1 tren kho du lieu trong bo nho (che do --no-db).
 * Moi test dung mot kho moi nen cac test khong anh huong lan nhau.
 *
 * @author Phung Minh Hien
 */
public class InMemoryUserRepositoryTest extends UserRepositoryTestBase {

    public InMemoryUserRepositoryTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        System.out.println("=== Kiem thu InMemoryUserRepository ===");
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @Override
    protected UserRepository createRepository() {
        return new InMemoryUserRepository();
    }
}
