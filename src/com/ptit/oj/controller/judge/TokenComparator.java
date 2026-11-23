package com.ptit.oj.controller.judge;

import java.util.Arrays;
import java.util.List;

public class TokenComparator implements OutputComparator {
    @Override
    public boolean matches(String expected, String actual) {
        List<String> e = tokens(expected);
        List<String> a = tokens(actual);
        return e.size() == a.size() && firstMismatch(e, a) < 0;
    }

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

    protected boolean tokenEquals(String expected, String actual) {
        return expected.equals(actual);
    }

    protected String mismatchLabel() {
        return "Lệch";
    }

    private List<String> tokens(String s) {
        String trimmed = s == null ? "" : s.trim();
        return trimmed.isEmpty() ? Arrays.asList() : Arrays.asList(trimmed.split("\\s+"));
    }

    private int firstMismatch(List<String> e, List<String> a) {
        for (int i = 0; i < e.size(); i++) {
            if (!tokenEquals(e.get(i), a.get(i))) return i;
        }
        return -1;
    }
}
