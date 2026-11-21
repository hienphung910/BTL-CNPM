package com.ptit.oj.controller.judge;

/**
 * So sánh output theo từng ký tự sau khi chuẩn hóa dấu xuống dòng Windows/Unix.
 * Khoảng trắng và số dòng còn lại phải khớp hoàn toàn.
 */
public class ExactComparator implements OutputComparator {

    /**
     * Chuẩn hóa ký tự xuống dòng rồi đối chiếu toàn bộ hai chuỗi.
     */
    @Override
    public boolean matches(String expected, String actual) {
        return normalize(expected).equals(normalize(actual));
    }

    /**
     * Đổi CRLF/CR thành LF và coi null là chuỗi rỗng.
     */
    private String normalize(String s) {
        return s == null ? "" : s.replace("\r\n", "\n").replace("\r", "\n");
    }
}
