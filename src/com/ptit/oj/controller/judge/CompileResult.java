package com.ptit.oj.controller.judge;

/**
 * Lưu kết quả biên dịch hoặc kiểm tra cú pháp: trạng thái thành công và thông báo lỗi.
 */
public class CompileResult {

    // Trạng thái và thông báo của bước biên dịch.
    private boolean success;
    private String message;

    /**
     * Khởi tạo kết quả biên dịch; chuyển thông báo null thành chuỗi rỗng.
     */
    private CompileResult(boolean success, String message) {
        this.success = success;
        this.message = message == null ? "" : message;
    }

    /**
     * Tạo kết quả biên dịch thành công, không có thông báo lỗi.
     */
    public static CompileResult ok() { return new CompileResult(true, ""); }

    /**
     * Tạo kết quả biên dịch thất bại kèm thông báo của trình biên dịch.
     */
    public static CompileResult fail(String message) { return new CompileResult(false, message); }

    /**
     * Cho biết bước biên dịch hoặc kiểm tra cú pháp có thành công hay không.
     */
    public boolean isSuccess() { return success; }

    /**
     * Trả thông báo kết quả.
     */
    public String getMessage() { return message; }
}
