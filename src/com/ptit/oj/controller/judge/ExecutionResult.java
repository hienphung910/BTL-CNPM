package com.ptit.oj.controller.judge;

/**
 * Lưu kết quả chạy tiến trình con: mã thoát, stdout, stderr, thời gian và trạng thái quá hạn.
 */
public class ExecutionResult {

    // Dữ liệu trả về sau khi theo dõi tiến trình con.
    private int exitCode;
    private String stdout;
    private String stderr;
    private long elapsedMs;
    private boolean timedOut;

    /**
     * Ghi nhận mã thoát, hai luồng output, thời gian chạy và cờ timeout.
     */
    public ExecutionResult(int exitCode, String stdout, String stderr, long elapsedMs, boolean timedOut) {
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
        this.elapsedMs = elapsedMs;
        this.timedOut = timedOut;
    }

    /**
     * Trả mã thoát của tiến trình.
     */
    public int getExitCode() { return exitCode; }

    /**
     * Trả output chuẩn đã thu được.
     */
    public String getStdout() { return stdout; }

    /**
     * Trả thông báo trên luồng lỗi.
     */
    public String getStderr() { return stderr; }

    /**
     * Trả thời gian tiến trình chạy, tính bằng mili giây.
     */
    public long getElapsedMs() { return elapsedMs; }

    /**
     * Cho biết tiến trình có vượt quá thời gian cho phép hay không.
     */
    public boolean isTimedOut() { return timedOut; }

    /**
     * Coi tiến trình thành công khi không timeout và có mã thoát bằng 0.
     */
    public boolean isSuccess() { return !timedOut && exitCode == 0; }
}
