package com.ptit.oj.controller.judge;

/**
 * Tạo bộ so sánh output từ cấu hình bài tập: exact, token, lines hoặc float[:epsilon].
 */
public class ComparatorFactory {

    /**
     * Ngăn tạo đối tượng vì lớp chỉ cung cấp hàm tạo bộ so sánh tĩnh.
     */
    private ComparatorFactory() { }

    /**
     * Chọn bộ so sánh từ chuỗi cấu hình; mặc định dùng token.
     * Với float, đọc epsilon sau dấu hai chấm hoặc dùng 1e-6; kiểu không hỗ trợ sẽ báo lỗi.
     */
    public static OutputComparator create(String spec) {
        if (spec == null || spec.trim().isEmpty()) return new TokenComparator();
        String s = spec.trim().toLowerCase();

        if (s.equals("exact")) return new ExactComparator();
        if (s.equals("token")) return new TokenComparator();
        if (s.equals("lines")) return new LineComparator();
        if (s.startsWith("float")) {
            double eps = 1e-6;
            int colon = s.indexOf(':');
            if (colon >= 0) {
                try {
                    eps = Double.parseDouble(s.substring(colon + 1).trim());
                } catch (NumberFormatException ignored) {
                    // Giữ epsilon mặc định nếu cấu hình không phân tích được thành số.
                }
            }
            return new FloatComparator(eps);
        }
        throw new IllegalArgumentException("Kiểu so sánh không hỗ trợ: " + spec);
    }
}
