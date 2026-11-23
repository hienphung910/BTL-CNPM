package com.ptit.oj.controller.judge;

public interface OutputComparator {
    boolean matches(String expected, String actual);

    default String explain(String expected, String actual) {
        return "Nhận được: [" + preview(actual) + "]";
    }

    static String preview(String s) {
        String flat = s == null ? "" : s.replace("\r", "").replace("\n", " / ").trim();
        return flat.length() <= 60 ? flat : flat.substring(0, 57) + "...";
    }
}
