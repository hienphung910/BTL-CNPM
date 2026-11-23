package com.ptit.oj.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

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
