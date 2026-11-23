package com.ptit.oj.dao;

import com.ptit.oj.model.Problem;
import com.ptit.oj.model.TestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class InMemoryProblemRepositoryTest {
    private Repository<Problem> instance;

    @BeforeEach
    public void setUp() {
        instance = new InMemoryRepository<>();
        instance.save(createProblem("K001", "Con vẹt Lóc Cóc nhại số"));
        instance.save(createProblem("K002", "Robot Bi chào bạn mới"));
    }

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

    @Test
    public void test03_FindById() {
        System.out.println("findById - ma bai ton tai va ma bai khong ton tai");

        Problem found = instance.findById("K002");
        assertNotNull(found);
        assertEquals("K002", found.getId());
        assertEquals("Robot Bi chào bạn mới", found.getTitle());

        assertNull(instance.findById("K999"));
    }

    private Problem createProblem(String id, String title) {
        Problem problem = new Problem(id, title, "", 1000, 128, "token");
        problem.addTestCase(new TestCase("sample01", "5", "5", 50, true));
        problem.addTestCase(new TestCase("01", "7", "7", 50, false));
        return problem;
    }
}
