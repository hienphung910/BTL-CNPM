/*
 * Kiem thu don vi (JUnit 5) cho kho bai tap trong bo nho (InMemoryRepository) - Module 2.
 * Moi phuong thuc test ung voi mot dong trong bang 6.1 cua bao cao, dung thu tu.
 */
package com.ptit.oj.dao;

import com.ptit.oj.model.Problem;
import com.ptit.oj.model.TestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiem thu kho bai tap ma chuong trinh dung sau khi doc bai tu o dia.
 * Moi test dung mot kho moi da co san 2 bai K001, K002.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class InMemoryProblemRepositoryTest {

    private Repository<Problem> instance;

    @BeforeEach
    public void setUp() {
        instance = new InMemoryRepository<>();
        instance.save(createProblem("K001", "Con vẹt Lóc Cóc nhại số"));
        instance.save(createProblem("K002", "Robot Bi chào bạn mới"));
    }

    /**
     * Test of save method, of class InMemoryRepository.
     * Luu bai moi: count() tang 1 va findById() tra ve dung bai.
     */
    @Test
    public void test01_Save_BaiMoi() {
        System.out.println("save - luu bai moi");
        Problem problem = createProblem("P900", "Tổng hai số");
        int before = instance.count();

        instance.save(problem);

        assertEquals(before + 1, instance.count());
        assertSame(problem, instance.findById("P900"));
        assertEquals(3, instance.findAll().size());
    }

    /**
     * Test of save method, of class InMemoryRepository.
     * Luu bai co ma trung: thay the bai cu, count() khong doi.
     */
    @Test
    public void test02_Save_MaTrung() {
        System.out.println("save - luu bai co ma trung");
        Problem replacement = createProblem("K001", "Tên mới của bài K001");
        int before = instance.count();

        instance.save(replacement);

        assertEquals(before, instance.count());
        assertSame(replacement, instance.findById("K001"));
        assertEquals("Tên mới của bài K001", instance.findById("K001").getTitle());
    }

    /**
     * Test of findById method, of class InMemoryRepository.
     * Ma bai ton tai: tra ve Problem; ma bai khong ton tai: tra ve null.
     */
    @Test
    public void test03_FindById() {
        System.out.println("findById - ma bai ton tai va ma bai khong ton tai");

        Problem found = instance.findById("K002");
        assertNotNull(found);
        assertEquals("K002", found.getId());
        assertEquals("Robot Bi chào bạn mới", found.getTitle());

        assertNull(instance.findById("K999"));
    }

    /** Bai tap co 2 test: sample01 la test vi du, 01 la test an, moi test 50 diem. */
    private Problem createProblem(String id, String title) {
        Problem problem = new Problem(id, title, "", 1000, 128, "token");
        problem.addTestCase(new TestCase("sample01", "5", "5", 50, true));
        problem.addTestCase(new TestCase("01", "7", "7", 50, false));
        return problem;
    }
}
