package com.ptit.oj;

import com.ptit.oj.controller.JudgeService;
import com.ptit.oj.dao.DataAccessException;
import com.ptit.oj.dao.DatabaseConfig;
import com.ptit.oj.dao.MySqlDatabaseManager;
import com.ptit.oj.dao.ProblemLoadException;
import com.ptit.oj.view.account.LoginView;

import javax.swing.UIManager;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static final String SEED_PASSWORD_ENV = "OJ_SEED_PASSWORD";
    private static final String DEFAULT_SEED_PASSWORD = "ptit@2026";

    public static void main(String[] args) {
        forceUtf8Output();

        boolean noDatabase = false;
        for (String arg : args) {
            if (arg.equals("--no-db")) {
                noDatabase = true;
            } else {
                System.out.println("Tham số không hiểu: " + arg);
            }
        }

        Path dataDir = Paths.get("data");
        if (!Files.isDirectory(dataDir)) {
            System.out.println("Không tìm thấy thư mục dữ liệu: " + dataDir.toAbsolutePath());
            System.out.println("Hãy chạy chương trình từ thư mục gốc của project (nơi có folder data).");
            return;
        }

        try {
            JudgeService service;
            if (noDatabase) {
                System.out.println("Chế độ --no-db: tài khoản và bài nộp chỉ nằm trong RAM, tắt chương trình là mất.");
                service = JudgeService.inMemory(dataDir);
            } else {
                MySqlDatabaseManager database = connectToMySql();
                if (database == null) return;
                service = JudgeService.mysql(dataDir, database);
            }
            service.loadData();
            prepareAccounts(service);
            System.out.printf("Đã nạp %d bài tập, %d tài khoản.%n",
                    service.getProblems().size(), service.getUsers().size());
            openLoginWindow(service);
        } catch (ProblemLoadException e) {
            System.out.println("Lỗi nạp đề bài: " + e.getMessage());
        } catch (DataAccessException e) {
            System.out.println("Lỗi cơ sở dữ liệu: " + e.getMessage());
        }
    }

    private static void openLoginWindow(JudgeService service) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        LoginView loginView = new LoginView(service);
        loginView.setVisible(true);
    }

    private static MySqlDatabaseManager connectToMySql() {
        DatabaseConfig config;
        try {
            config = DatabaseConfig.fromEnvironment();
        } catch (DatabaseConfig.MissingConfigException e) {
            System.out.println(e.getMessage());
            return null;
        }

        MySqlDatabaseManager database = new MySqlDatabaseManager(
                config.getUrl(), config.getUser(), config.getPassword());
        try {
            database.testConnection();
        } catch (DataAccessException e) {
            System.out.println("Không kết nối được MySQL.");
            System.out.println("  " + e.getMessage());
            System.out.println("  Xem hướng dẫn tạo cơ sở dữ liệu ở database/mysql/01_create_database.sql");
            return null;
        }
        System.out.println("MySQL: " + database.getSafeUrl() + " (user " + database.getUser() + ")");
        return database;
    }

    private static void prepareAccounts(JudgeService service) {
        String seedPassword = System.getenv(SEED_PASSWORD_ENV);
        if (seedPassword == null || seedPassword.trim().isEmpty()) {
            seedPassword = DEFAULT_SEED_PASSWORD;
        }
        if (service.getAuth().seedSampleUsersIfEmpty(seedPassword)) {
            System.out.println("Đã tạo 5 tài khoản mẫu: admin, gvthanh, hiennm, lananh, tuanpv.");
            System.out.println("  Mật khẩu mẫu: " + seedPassword
                    + "   (đặt biến môi trường " + SEED_PASSWORD_ENV + " để dùng mật khẩu khác)");
        }
    }

    private static void forceUtf8Output() {
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
        } catch (UnsupportedEncodingException ignored) {
        }
    }
}
