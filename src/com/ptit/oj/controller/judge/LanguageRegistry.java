package com.ptit.oj.controller.judge;

import java.util.Arrays;
import java.util.List;

/**
 * Quản lý danh sách ngôn ngữ mà hệ thống hỗ trợ và tìm ngôn ngữ theo tên.
 * Muốn hỗ trợ ngôn ngữ mới, thêm một lớp con của Language vào danh sách này.
 */
public class LanguageRegistry {

    // Đăng ký bốn ngôn ngữ hiện có của hệ thống.
    private List<Language> languages = Arrays.asList(
            new JavaLanguage(),
            new CppLanguage(),
            new PythonLanguage(),
            new JavaScriptLanguage());

    /**
     * Trả danh sách các đối tượng ngôn ngữ đã đăng ký.
     */
    public List<Language> all() {
        return languages;
    }

    /**
     * Tìm ngôn ngữ không phân biệt chữ hoa/thường; trả null nếu không tìm thấy.
     */
    public Language byName(String name) {
        for (Language l : languages) {
            if (l.getName().equalsIgnoreCase(name)) return l;
        }
        return null;
    }
}
