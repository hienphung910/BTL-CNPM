package com.ptit.oj.controller.judge;

import java.util.Arrays;
import java.util.List;

/**
 * So sánh từng token, bỏ qua khác biệt về khoảng trắng và xuống dòng.
 * FloatComparator kế thừa vòng lặp so sánh và chỉ thay đổi cách đối chiếu một token.
 */
public class TokenComparator implements OutputComparator {

    /**
     * Kiểm tra số lượng token bằng nhau và không có token nào khác nhau.
     */
    @Override
    public boolean matches(String expected, String actual) {
        List<String> e = tokens(expected);
        List<String> a = tokens(actual);
        return e.size() == a.size() && firstMismatch(e, a) < 0;
    }

    /**
     * Thông báo số lượng hoặc vị trí token sai, chỉ kèm giá trị người nộp đã in.
     */
    @Override
    public String explain(String expected, String actual) {
        List<String> e = tokens(expected);
        List<String> a = tokens(actual);
        if (e.size() != a.size()) {
            return "Số lượng giá trị không khớp: nhận được " + a.size();
        }
        int i = firstMismatch(e, a);
        if (i < 0) return "Khớp";
        return mismatchLabel() + " tại giá trị thứ " + (i + 1) + ": nhận được [" + a.get(i) + "]";
    }

    /**
     * So sánh chính xác một cặp token; lớp con có thể thay bằng quy tắc sai số.
     */
    protected boolean tokenEquals(String expected, String actual) {
        return expected.equals(actual);
    }

    /**
     * Cung cấp nhãn cho thông báo token khác nhau.
     */
    protected String mismatchLabel() {
        return "Lệch";
    }

    /**
     * Loại khoảng trắng hai đầu và tách chuỗi theo một hoặc nhiều ký tự trắng.
     */
    private List<String> tokens(String s) {
        String trimmed = s == null ? "" : s.trim();
        return trimmed.isEmpty() ? Arrays.asList() : Arrays.asList(trimmed.split("\\s+"));
    }

    /**
     * Tìm chỉ số token sai đầu tiên; trả -1 khi khớp hết. Hai danh sách phải có cùng độ dài.
     */
    private int firstMismatch(List<String> e, List<String> a) {
        for (int i = 0; i < e.size(); i++) {
            if (!tokenEquals(e.get(i), a.get(i))) return i;
        }
        return -1;
    }
}
