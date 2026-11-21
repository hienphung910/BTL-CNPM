package com.ptit.oj.controller.judge;

import java.util.ArrayList;
import java.util.List;

/**
 * So sánh output theo từng dòng, phù hợp với bài vẽ hình bằng ký tự.
 * Giữ khoảng trắng đầu dòng; bỏ khoảng trắng cuối dòng và các dòng rỗng ở cuối output.
 */
public class LineComparator implements OutputComparator {

    /**
     * Chuẩn hóa mỗi output thành danh sách dòng rồi so sánh hai danh sách.
     */
    @Override
    public boolean matches(String expected, String actual) {
        return lines(expected).equals(lines(actual));
    }

    /**
     * Thông báo khác số dòng hoặc dòng sai đầu tiên, không tiết lộ dòng đáp án.
     */
    @Override
    public String explain(String expected, String actual) {
        List<String> e = lines(expected);
        List<String> a = lines(actual);
        if (e.size() != a.size()) {
            return "Số dòng không khớp: nhận được " + a.size() + " dòng";
        }
        for (int i = 0; i < e.size(); i++) {
            if (!e.get(i).equals(a.get(i))) {
                return "Lệch tại dòng thứ " + (i + 1) + ": nhận được [" + a.get(i) + "]";
            }
        }
        return "Khớp";
    }

    /**
     * Chuẩn hóa xuống dòng, bỏ khoảng trắng cuối mỗi dòng và bỏ các dòng rỗng cuối output.
     */
    private List<String> lines(String s) {
        List<String> result = new ArrayList<>();
        if (s == null) return result;
        for (String line : s.replace("\r\n", "\n").replace("\r", "\n").split("\n", -1)) {
            result.add(line.stripTrailing());
        }
        while (!result.isEmpty() && result.get(result.size() - 1).isEmpty()) {
            result.remove(result.size() - 1);
        }
        return result;
    }
}
