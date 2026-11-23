package com.ptit.oj.controller.judge;

public class ExactComparator implements OutputComparator {
    @Override
    public boolean matches(String expected, String actual) {
        return normalize(expected).equals(normalize(actual));
    }

    private String normalize(String s) {
        return s == null ? "" : s.replace("\r\n", "\n").replace("\r", "\n");
    }
}
