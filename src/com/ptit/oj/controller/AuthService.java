package com.ptit.oj.controller;

import com.ptit.oj.dao.UserRepository;
import com.ptit.oj.model.Admin;
import com.ptit.oj.model.Credentials;
import com.ptit.oj.model.Student;
import com.ptit.oj.model.Teacher;
import com.ptit.oj.model.User;

import java.util.Locale;
import java.util.UUID;

public class AuthService {
    private static final int MAX_FULL_NAME = 100;
    private static final int MAX_CODE = 40;
    private static final String INVALID_LOGIN = "Sai tên đăng nhập hoặc mật khẩu";

    private UserRepository users;
    private PasswordService passwords;
    private SessionService sessions;

    public AuthService(UserRepository users, PasswordService passwords, SessionService sessions) {
        this.users = users;
        this.passwords = passwords;
        this.sessions = sessions;
    }

    public static class LoginResult {
        private String token;
        private User user;

        LoginResult(String token, User user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() { return token; }
        public User getUser() { return user; }
    }

    public static class AuthException extends RuntimeException {
        public AuthException(String message) {
            super(message);
        }
    }

    public User register(String username, String rawPassword, String fullName,
                                      String studentCode, String className) {
        String cleanUsername = normalizeOrFail(username);
        passwords.validate(rawPassword);

        String cleanFullName = requireText(fullName, "Họ tên", MAX_FULL_NAME);
        String cleanCode = optionalText(studentCode, "Mã sinh viên", MAX_CODE);
        String cleanClass = optionalText(className, "Lớp", MAX_CODE);

        requireNewUsername(cleanUsername);

        Student student = new Student(newUserId(), cleanUsername, cleanFullName, cleanCode, cleanClass);
        users.saveWithCredentials(student, passwords.hash(rawPassword));
        return student;
    }

    public User createAccount(User user, String rawPassword) {
        passwords.validate(rawPassword);
        requireNewUsername(user.getUsername());
        users.saveWithCredentials(user, passwords.hash(rawPassword));
        return user;
    }

    public LoginResult login(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty() || rawPassword == null || rawPassword.isEmpty()) {
            throw new AuthException("Thiếu tên đăng nhập hoặc mật khẩu");
        }
        User user = users.findByUsername(username.trim());
        if (user == null) {
            throw new AuthException(INVALID_LOGIN);
        }
        Credentials credentials = users.findCredentials(user.getId());
        if (credentials == null || !passwords.matches(rawPassword, credentials)) {
            throw new AuthException(INVALID_LOGIN);
        }
        return new LoginResult(sessions.createToken(user.getId()), user);
    }

    public User authenticate(String token) {
        String userId = sessions.resolve(token);
        if (userId == null) return null;
        return users.findById(userId);
    }

    public boolean logout(String token) {
        return sessions.invalidate(token);
    }

    public boolean seedSampleUsersIfEmpty(String rawPassword) {
        if (users.count() > 0) return false;
        createAccount(new Admin(newUserId(), "admin", "Quản trị hệ thống"), rawPassword);
        createAccount(new Teacher(newUserId(), "gvthanh", "Lê Minh Thành", "Khoa học máy tính"), rawPassword);
        createAccount(new Student(newUserId(), "hiennm", "Nguyễn Minh Hiển", "B24DCCN199", "D24CQCN01"), rawPassword);
        createAccount(new Student(newUserId(), "lananh", "Trần Lan Anh", "B24DCCN042", "D24CQCN01"), rawPassword);
        createAccount(new Student(newUserId(), "tuanpv", "Phạm Văn Tuấn", "B24DCCN311", "D24CQCN02"), rawPassword);
        return true;
    }

    private static String newUserId() {
        return "U-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
    }

    private void requireNewUsername(String username) {
        if (users.existsByUsername(username)) {
            throw new AuthException("Tên đăng nhập \"" + username + "\" đã tồn tại");
        }
    }

    private String normalizeOrFail(String username) {
        try {
            return PasswordService.normalizeUsername(username);
        } catch (IllegalArgumentException e) {
            throw new AuthException(e.getMessage());
        }
    }

    private String requireText(String value, String label, int max) {
        String v = value == null ? "" : value.trim();
        if (v.isEmpty()) throw new AuthException(label + " không được để trống");
        if (v.length() > max) throw new AuthException(label + " tối đa " + max + " ký tự");
        return v;
    }

    private String optionalText(String value, String label, int max) {
        String v = value == null ? "" : value.trim();
        if (v.length() > max) throw new AuthException(label + " tối đa " + max + " ký tự");
        return v;
    }
}
