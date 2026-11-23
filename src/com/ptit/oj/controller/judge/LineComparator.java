package com.ptit.oj.controller.judge;

import java.util.ArrayList;
import java.util.List;

public class LineComparator implements OutputComparator {
    @Override
    public boolean matches(String expected, String actual) {
        return lines(expected).equals(lines(actual));
    }

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
