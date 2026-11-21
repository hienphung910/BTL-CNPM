package com.ptit.oj.model;

/**
 * Liệt kê các trạng thái kết quả chấm cùng mã hiển thị và mức độ ưu tiên.
 * Giá trị severity càng lớn càng được ưu tiên khi tổng hợp verdict của nhiều test.
 */
public enum Verdict {
    // Đáp án được chấp nhận.
    AC("AC", "Accepted", 0),
    // Lỗi trình bày, được khai báo dự phòng và chưa được bộ chấm sinh ra.
    PE("PE", "Presentation Error", 1),      // Dự phòng: bộ chấm hiện chưa sinh verdict này.
    // Output không khớp đáp án theo bộ so sánh.
    WA("WA", "Wrong Answer", 2),
    // Chương trình vượt giới hạn thời gian.
    TLE("TLE", "Time Limit Exceeded", 3),
    // Runtime báo dấu hiệu vượt bộ nhớ.
    MLE("MLE", "Memory Limit Exceeded", 4),
    // Chương trình kết thúc với lỗi thực thi.
    RE("RE", "Runtime Error", 5),
    // Biên dịch hoặc kiểm tra cú pháp thất bại.
    CE("CE", "Compile Error", 6),
    // Lỗi nội bộ, thiếu toolchain hoặc chưa có test để chấm.
    IE("IE", "Internal Error", 7);

    // Mã trạng thái, tên mô tả và mức ưu tiên khi tổng hợp kết quả.
    private String code;
    private String display;
    private int severity;

    /**
     * Gắn mã ngắn, tên hiển thị và mức độ ưu tiên cho từng giá trị enum.
     */
    Verdict(String code, String display, int severity) {
        this.code = code;
        this.display = display;
        this.severity = severity;
    }

    /**
     * Trả mã ngắn của verdict.
     */
    public String getCode() { return code; }

    /**
     * Trả tên mô tả verdict.
     */
    public String getDisplay() { return display; }

    /**
     * Trả mức ưu tiên của verdict khi tổng hợp kết quả.
     */
    public int getSeverity() { return severity; }

    /**
     * Chỉ trả true với trạng thái AC.
     */
    public boolean isAccepted() { return this == AC; }
}
