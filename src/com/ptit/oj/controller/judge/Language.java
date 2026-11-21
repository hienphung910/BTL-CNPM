package com.ptit.oj.controller.judge;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Định nghĩa hợp đồng chung cho các ngôn ngữ: kiểm tra toolchain, biên dịch và tạo lệnh chạy.
 * Cung cấp tiện ích dùng chung; mỗi lớp con triển khai lệnh tương ứng với ngôn ngữ của mình.
 */
public abstract class Language {

    // Giới hạn biên dịch và công cụ chạy tiến trình; cache tránh kiểm tra toolchain lặp lại.
    private static final long COMPILE_TIME_LIMIT_MS = 20_000;

    protected ProcessRunner runner = new ProcessRunner();
    private Boolean availableCache;

    /**
     * Cung cấp tên ngôn ngữ để hiển thị và tra cứu.
     */
    public abstract String getName();

    /**
     * Cung cấp phần mở rộng của file mã nguồn, ví dụ .java.
     */
    public abstract String getFileExtension();

    /**
     * Cung cấp lệnh kiểm tra phiên bản compiler hoặc interpreter.
     */
    protected abstract List<String> getVersionCommand();

    /**
     * Biên dịch mã nguồn; với ngôn ngữ thông dịch, thực hiện bước kiểm tra cú pháp.
     */
    public abstract CompileResult compile(Path source, Path workDir) throws IOException, InterruptedException;

    /**
     * Tạo danh sách tham số chạy bài nộp theo ngôn ngữ và cấu hình bộ nhớ.
     */
    public abstract List<String> buildRunCommand(Path source, Path workDir, int memoryLimitMb);

    /**
     * Trả thời gian bù khởi động; mặc định không cộng thêm.
     */
    public long getStartupOverheadMs() { return 0; }

    /**
     * Đặt tên file mặc định là solution cộng phần mở rộng; Java ghi đè quy tắc này.
     */
    public String defaultFileName(String sourceCode) {
        return "solution" + getFileExtension();
    }

    /**
     * Thử lệnh phiên bản một lần và lưu kết quả; hiện chỉ kiểm tra lệnh chạy được và không timeout.
     */
    public boolean isAvailable() {
        if (availableCache == null) {
            ExecutionResult r = probe(getVersionCommand());
            availableCache = r != null && !r.isTimedOut();
        }
        return availableCache;
    }

    /**
     * Thử chạy lệnh trong tối đa 10 giây; trả null nếu có lỗi I/O hoặc bị ngắt.
     */
    protected ExecutionResult probe(List<String> command) {
        try {
            return runner.run(command, Paths.get(System.getProperty("user.dir")), "", 10_000);
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    /**
     * Chạy bước biên dịch tối đa 20 giây; chuyển timeout hoặc mã thoát lỗi thành CompileResult thất bại.
     */
    protected CompileResult runCompiler(Path workDir, List<String> command) throws IOException, InterruptedException {
        ExecutionResult r = runner.run(command, workDir, "", COMPILE_TIME_LIMIT_MS);
        if (r.isTimedOut()) return CompileResult.fail("Biên dịch quá lâu (> 20s)");
        if (r.getExitCode() != 0) {
            String message = r.getStderr().isEmpty() ? r.getStdout() : r.getStderr();
            return CompileResult.fail(message.trim());
        }
        return CompileResult.ok();
    }

    /**
     * Tạo đường dẫn file thực thi C++ với đuôi .exe trên Windows hoặc .bin trên hệ khác.
     */
    protected static Path executablePath(Path source, Path workDir) {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        return workDir.resolve(baseName(source) + (windows ? ".exe" : ".bin"));
    }

    /**
     * Lấy tên file bỏ phần mở rộng để dùng làm tên class hoặc tên file thực thi.
     */
    protected static String baseName(Path source) {
        String name = source.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }

    /**
     * Dùng tên ngôn ngữ làm chuỗi biểu diễn của đối tượng.
     */
    @Override
    public String toString() { return getName(); }
}
