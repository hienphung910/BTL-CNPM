package com.ptit.oj.controller;

import com.ptit.oj.model.Credentials;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

public class PasswordService {
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 128;
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 32;
    private static final String USERNAME_PATTERN = "[A-Za-z0-9_.]+";

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private SecureRandom random = new SecureRandom();
    private int iterations = 120_000;

    public void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được rỗng");
        }
        if (rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự");
        }
        if (rawPassword.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Mật khẩu tối đa " + MAX_PASSWORD_LENGTH + " ký tự");
        }
    }

    public Credentials hash(String rawPassword) {
        validate(rawPassword);
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        byte[] hash = pbkdf2(rawPassword.toCharArray(), salt, iterations);
        try {
            return new Credentials(
                    Base64.getEncoder().encodeToString(hash),
                    Base64.getEncoder().encodeToString(salt),
                    iterations);
        } finally {
            Arrays.fill(hash, (byte) 0);
        }
    }

    public boolean matches(String rawPassword, Credentials stored) {
        if (rawPassword == null || stored == null) return false;
        if (rawPassword.length() > MAX_PASSWORD_LENGTH) return false;
        byte[] expected;
        byte[] salt;
        try {
            expected = Base64.getDecoder().decode(stored.getHashBase64());
            salt = Base64.getDecoder().decode(stored.getSaltBase64());
        } catch (IllegalArgumentException e) {
            return false;
        }
        byte[] actual = pbkdf2(rawPassword.toCharArray(), salt, stored.getIterations());
        try {
            return MessageDigest.isEqual(expected, actual);
        } finally {
            Arrays.fill(actual, (byte) 0);
        }
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int rounds) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, rounds, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("JVM không hỗ trợ " + ALGORITHM, e);
        } finally {
            spec.clearPassword();
            Arrays.fill(password, '\0');
        }
    }

    public static String normalizeUsername(String username) {
        String u = username == null ? "" : username.trim();
        if (u.length() < MIN_USERNAME_LENGTH || u.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException("Tên đăng nhập phải dài "
                    + MIN_USERNAME_LENGTH + "-" + MAX_USERNAME_LENGTH + " ký tự");
        }
        if (!u.matches(USERNAME_PATTERN)) {
            throw new IllegalArgumentException("Tên đăng nhập chỉ gồm chữ, số, dấu chấm hoặc gạch dưới");
        }
        return u;
    }
}
