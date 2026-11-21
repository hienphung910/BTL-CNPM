/*
 * Kiem thu don vi (JUnit 5) cho tang DAO cua Module 1 - Quan ly tai khoan va phan quyen.
 * Moi phuong thuc test ung voi mot dong trong bang 6.1 cua bao cao, dung thu tu.
 */
package com.ptit.oj.dao;

import com.ptit.oj.controller.PasswordService;
import com.ptit.oj.controller.SessionService;
import com.ptit.oj.model.Admin;
import com.ptit.oj.model.Credentials;
import com.ptit.oj.model.Student;
import com.ptit.oj.model.Teacher;
import com.ptit.oj.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Cac test dung chung cho moi lop cai dat UserRepository.
 * Lop con chi can cho biet dung kho du lieu nao (bo nho hay MySQL).
 *
 * @author Phung Minh Hien
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class UserRepositoryTestBase {

    /** Mat khau chung cua 5 tai khoan mau, giong Main.DEFAULT_SEED_PASSWORD. */
    protected static final String SEED_PASSWORD = "ptit@2026";

    private static Credentials seedCredentials;

    protected UserRepository instance;

    /** Ma cac tai khoan do chinh test tao ra, xoa o tearDown() de CSDL tro lai nhu cu. */
    private final List<String> createdIds = new ArrayList<>();

    /** Lop con tra ve kho du lieu can kiem thu. */
    protected abstract UserRepository createRepository();

    @BeforeEach
    public void setUp() {
        instance = createRepository();
        seedSampleUsersIfMissing();
    }

    @AfterEach
    public void tearDown() {
        for (String id : createdIds) {
            instance.deleteById(id);
        }
        createdIds.clear();
    }

    // ================================================================
    // 1. Dang ky tai khoan - existsByUsername(), saveWithCredentials()
    // ================================================================

    /**
     * Test of existsByUsername method, of class UserRepository.
     * Ten dang nhap da co (lananh): tra ve true.
     */
    @Test
    public void test01_ExistsByUsername_TenDaCo() {
        System.out.println("existsByUsername - ten dang nhap da co (lananh)");
        String username = "lananh";
        boolean expResult = true;
        boolean result = instance.existsByUsername(username);
        assertEquals(expResult, result);
    }

    /**
     * Test of existsByUsername method, of class UserRepository.
     * Cung ten nhung khac hoa thuong (LanAnh): van tra ve true.
     */
    @Test
    public void test02_ExistsByUsername_KhacHoaThuong() {
        System.out.println("existsByUsername - khac hoa thuong (LanAnh)");
        String username = "LanAnh";
        boolean expResult = true;
        boolean result = instance.existsByUsername(username);
        assertEquals(expResult, result);
    }

    /**
     * Test of existsByUsername method, of class UserRepository.
     * Ten dang nhap chua co (ducdm): tra ve false.
     */
    @Test
    public void test03_ExistsByUsername_TenChuaCo() {
        System.out.println("existsByUsername - ten dang nhap chua co (ducdm)");
        String username = "ducdm";
        boolean expResult = false;
        boolean result = instance.existsByUsername(username);
        assertEquals(expResult, result);
    }

    /**
     * Test of saveWithCredentials method, of class UserRepository.
     * Luu tai khoan moi: count() tang them 1, findByUsername() tim thay tai khoan vua luu.
     */
    @Test
    public void test04_SaveWithCredentials_LuuTaiKhoanMoi() {
        System.out.println("saveWithCredentials - luu tai khoan moi (ducdm)");
        Student user = new Student(newTestId(), "ducdm", "Đỗ Minh Đức", "B24DCCN215", "D24CQCN02");
        Credentials credentials = seedCredentials();
        int countBefore = instance.count();

        instance.saveWithCredentials(user, credentials);
        createdIds.add(user.getId());

        assertEquals(countBefore + 1, instance.count());
        User result = instance.findByUsername("ducdm");
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertTrue(result instanceof Student);
        assertEquals("Đỗ Minh Đức", result.getFullName());
        assertEquals("B24DCCN215", ((Student) result).getStudentCode());
        assertEquals("D24CQCN02", ((Student) result).getClassName());
    }

    /**
     * Test of saveWithCredentials method, of class UserRepository.
     * Luu kem Credentials: findCredentials() tra ve dung chuoi bam, muoi va so vong lap da luu.
     */
    @Test
    public void test05_SaveWithCredentials_LuuKemCredentials() {
        System.out.println("saveWithCredentials - luu kem Credentials");
        Student user = new Student(newTestId(), "ducdm", "Đỗ Minh Đức", "B24DCCN215", "D24CQCN02");
        Credentials expResult = new PasswordService().hash("duc@2026");

        instance.saveWithCredentials(user, expResult);
        createdIds.add(user.getId());

        Credentials result = instance.findCredentials(user.getId());
        assertNotNull(result);
        assertEquals(expResult.getHashBase64(), result.getHashBase64());
        assertEquals(expResult.getSaltBase64(), result.getSaltBase64());
        assertEquals(expResult.getIterations(), result.getIterations());
    }

    // ================================================================
    // 2. Dang nhap - findByUsername(), findCredentials()
    // ================================================================

    /**
     * Test of findByUsername method, of class UserRepository.
     * Ten dang nhap ton tai (lananh): tra ve Student dung ho ten, ma sinh vien va lop.
     */
    @Test
    public void test06_FindByUsername_TonTai() {
        System.out.println("findByUsername - ten dang nhap ton tai (lananh)");
        String username = "lananh";
        User result = instance.findByUsername(username);
        assertNotNull(result);
        assertTrue(result instanceof Student);
        assertEquals("lananh", result.getUsername());
        assertEquals("Trần Lan Anh", result.getFullName());
        assertEquals("B24DCCN042", ((Student) result).getStudentCode());
        assertEquals("D24CQCN01", ((Student) result).getClassName());
    }

    /**
     * Test of findByUsername method, of class UserRepository.
     * Viet hoa (LANANH) hoac thua dau cach o hai dau: van tra ve tai khoan lananh.
     */
    @Test
    public void test07_FindByUsername_VietHoaHoacThuaDauCach() {
        System.out.println("findByUsername - viet hoa (LANANH) va thua dau cach");
        User expResult = instance.findByUsername("lananh");
        assertNotNull(expResult);

        User upper = instance.findByUsername("LANANH");
        User spaced = instance.findByUsername("  lananh  ");

        assertNotNull(upper);
        assertNotNull(spaced);
        assertEquals(expResult.getId(), upper.getId());
        assertEquals(expResult.getId(), spaced.getId());
    }

    /**
     * Test of findByUsername method, of class UserRepository.
     * Ten dang nhap khong ton tai: tra ve null.
     */
    @Test
    public void test08_FindByUsername_KhongTonTai() {
        System.out.println("findByUsername - ten dang nhap khong ton tai");
        String username = "khongtontai";
        User result = instance.findByUsername(username);
        assertNull(result);
    }

    /**
     * Test of findCredentials method, of class UserRepository.
     * Ma tai khoan co thong tin xac thuc: tra ve Credentials gom chuoi bam, muoi va
     * so vong lap 120000, khong co mat khau goc.
     */
    @Test
    public void test09_FindCredentials_CoThongTinXacThuc() {
        System.out.println("findCredentials - ma tai khoan co thong tin xac thuc (lananh)");
        User lananh = instance.findByUsername("lananh");
        assertNotNull(lananh);

        Credentials result = instance.findCredentials(lananh.getId());

        assertNotNull(result);
        assertEquals(44, result.getHashBase64().length());   // 32 byte PBKDF2 -> 44 ky tu Base64
        assertEquals(24, result.getSaltBase64().length());   // 16 byte muoi  -> 24 ky tu Base64
        assertEquals(120000, result.getIterations());
        assertNotEquals(SEED_PASSWORD, result.getHashBase64());
        assertFalse(result.getHashBase64().contains(SEED_PASSWORD));
    }

    /**
     * Test of findCredentials method, of class UserRepository.
     * Ma tai khoan khong ton tai: tra ve null.
     */
    @Test
    public void test10_FindCredentials_KhongTonTai() {
        System.out.println("findCredentials - ma tai khoan khong ton tai");
        String userId = "U-KHONG-TON-TAI";
        Credentials result = instance.findCredentials(userId);
        assertNull(result);
    }

    // ================================================================
    // 3. Kiem tra phien va phan quyen - findById()
    // ================================================================

    /**
     * Test of findById method, of class UserRepository.
     * Ma tai khoan lay tu phien con han: tra ve dung tai khoan cung vai tro.
     */
    @Test
    public void test11_FindById_MaLayTuPhienConHan() {
        System.out.println("findById - ma tai khoan lay tu phien con han (admin, gvthanh)");
        SessionService sessions = new SessionService();

        String adminToken = sessions.createToken(instance.findByUsername("admin").getId());
        User admin = instance.findById(sessions.resolve(adminToken));
        assertNotNull(admin);
        assertTrue(admin instanceof Admin);
        assertEquals("admin", admin.getUsername());
        assertEquals(User.ROLE_ADMIN, admin.getRoleCode());
        assertTrue(admin.canCreateProblem());

        String teacherToken = sessions.createToken(instance.findByUsername("gvthanh").getId());
        User teacher = instance.findById(sessions.resolve(teacherToken));
        assertNotNull(teacher);
        assertTrue(teacher instanceof Teacher);
        assertEquals("gvthanh", teacher.getUsername());
        assertEquals(User.ROLE_TEACHER, teacher.getRoleCode());
        assertFalse(teacher.canCreateProblem());
    }

    /**
     * Test of findById method, of class UserRepository.
     * Ma tai khoan khong ton tai: tra ve null.
     */
    @Test
    public void test12_FindById_KhongTonTai() {
        System.out.println("findById - ma tai khoan khong ton tai");
        String id = "U-KHONG-TON-TAI";
        User result = instance.findById(id);
        assertNull(result);
    }

    // ================================================================
    // Du lieu mau - giong 5 tai khoan ma Main tao o lan chay dau tien
    // ================================================================

    /**
     * Them 5 tai khoan mau neu kho chua co. Tai khoan nao da co thi giu nguyen,
     * khong ghi de (saveWithCredentials cua MySQL dung ON DUPLICATE KEY UPDATE).
     */
    private void seedSampleUsersIfMissing() {
        seedIfMissing(new Admin(newTestId(), "admin", "Quản trị hệ thống"));
        seedIfMissing(new Teacher(newTestId(), "gvthanh", "Lê Minh Thành", "Khoa học máy tính"));
        seedIfMissing(new Student(newTestId(), "hiennm", "Nguyễn Minh Hiển", "B24DCCN199", "D24CQCN01"));
        seedIfMissing(new Student(newTestId(), "lananh", "Trần Lan Anh", "B24DCCN042", "D24CQCN01"));
        seedIfMissing(new Student(newTestId(), "tuanpv", "Phạm Văn Tuấn", "B24DCCN311", "D24CQCN02"));
    }

    private void seedIfMissing(User user) {
        if (!instance.existsByUsername(user.getUsername())) {
            instance.saveWithCredentials(user, seedCredentials());
        }
    }

    /** Bam mat khau mau mot lan roi dung lai, vi PBKDF2 120000 vong kha cham. */
    private static synchronized Credentials seedCredentials() {
        if (seedCredentials == null) {
            seedCredentials = new PasswordService().hash(SEED_PASSWORD);
        }
        return seedCredentials;
    }

    private static String newTestId() {
        return "U-TEST-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
    }
}
