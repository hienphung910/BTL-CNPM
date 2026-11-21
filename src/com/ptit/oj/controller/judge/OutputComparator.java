package com.ptit.oj.controller.judge;

/**
 * Định nghĩa cách so sánh output bài nộp với đáp án theo mẫu thiết kế Strategy.
 * Judge sử dụng giao diện này để thay đổi cách so sánh mà không phụ thuộc lớp cụ thể.
 */
public interface OutputComparator {

    /**
     * Trả true nếu output thực tế được coi là khớp với đáp án theo quy tắc của bộ so sánh.
     */
    boolean matches(String expected, String actual);

    /**
     * Tạo thông báo từ output người nộp; không đưa đáp án kỳ vọng của test vào thông báo.
     */
    default String explain(String expected, String actual) {
        return "Nhận được: [" + preview(actual) + "]";
    }

    /**
     * Gộp output thành một dòng và rút gọn còn tối đa 60 ký tự để hiển thị.
     */
    static String preview(String s) {
        String flat = s == null ? "" : s.replace("\r", "").replace("\n", " / ").trim();
        return flat.length() <= 60 ? flat : flat.substring(0, 57) + "...";
    }
}
