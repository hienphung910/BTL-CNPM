package com.ptit.oj.controller.judge;

/**
 * So sánh token số thực bằng sai số tuyệt đối hoặc tương đối cho phép.
 * Tái sử dụng việc tách token và tìm vị trí sai từ TokenComparator.
 */
public class FloatComparator extends TokenComparator {

    // Ngưỡng sai số dùng trong phép so sánh số thực.
    private double epsilon;

    /**
     * Lưu epsilon do cấu hình truyền vào làm ngưỡng so sánh số thực.
     */
    public FloatComparator(double epsilon) {
        this.epsilon = epsilon;
    }

    /**
     * Chấp nhận token giống hệt; nếu khác thì phân tích số và kiểm tra sai số tuyệt đối hoặc tương đối.
     * Nếu token khác nhau và không phân tích được thành số, trả false.
     */
    @Override
    protected boolean tokenEquals(String e, String a) {
        if (e.equals(a)) return true;
        try {
            double x = Double.parseDouble(e);
            double y = Double.parseDouble(a);
            double diff = Math.abs(x - y);
            // Chấp nhận sai số tuyệt đối hoặc sai số tương đối.
            return diff <= epsilon || diff <= epsilon * Math.max(Math.abs(x), Math.abs(y));
        } catch (NumberFormatException ex) {
            return false;   // Token khác nhau và không phải số được coi là không khớp.
        }
    }

    /**
     * Đổi nhãn thông báo thành lỗi lệch quá sai số.
     */
    @Override
    protected String mismatchLabel() {
        return "Lệch quá sai số";
    }
}
