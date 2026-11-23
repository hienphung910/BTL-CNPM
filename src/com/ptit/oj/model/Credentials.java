package com.ptit.oj.model;

public class Credentials {
    private String hashBase64;
    private String saltBase64;
    private int iterations;

    public Credentials(String hashBase64, String saltBase64, int iterations) {
        if (hashBase64 == null || hashBase64.isEmpty()) {
            throw new IllegalArgumentException("Thiếu chuỗi băm mật khẩu");
        }
        if (saltBase64 == null || saltBase64.isEmpty()) {
            throw new IllegalArgumentException("Thiếu muối băm mật khẩu");
        }
        if (iterations <= 0) {
            throw new IllegalArgumentException("Số vòng lặp phải dương");
        }
        this.hashBase64 = hashBase64;
        this.saltBase64 = saltBase64;
        this.iterations = iterations;
    }

    public String getHashBase64() { return hashBase64; }
    public String getSaltBase64() { return saltBase64; }
    public int getIterations() { return iterations; }
}
