package com.ptit.oj.dao;

public class TextUtils {
    private static final char BOM = 0xFEFF;

    private TextUtils() { }

    public static String stripBom(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) == BOM ? s.substring(1) : s;
    }
}
