package com.ptit.oj.controller.judge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Cài đặt cách chấm Python: chọn python/python3, kiểm tra cú pháp rồi chạy mã nguồn.
 * Lệnh chạy hiện tại chưa áp đặt giới hạn bộ nhớ của tiến trình.
 */
public class PythonLanguage extends Language {

    // Chọn tên interpreter mặc định theo hệ điều hành.
    private String interpreter = System.getProperty("os.name").toLowerCase().contains("win") ? "python" : "python3";

    /**
     * Trả tên ngôn ngữ để hiển thị và tra cứu.
     */
    @Override public String getName() { return "Python"; }

    /**
     * Trả phần mở rộng của file mã nguồn tương ứng.
     */
    @Override public String getFileExtension() { return ".py"; }

    /**
     * Trả số mili giây bù thêm cho thời gian khởi động runtime.
     */
    @Override public long getStartupOverheadMs() { return 300; }

    /**
     * Tạo lệnh kiểm tra phiên bản toolchain của ngôn ngữ.
     */
    @Override
    protected List<String> getVersionCommand() {
        return Arrays.asList(interpreter, "--version");
    }

    /**
     * Thử tên interpreter mặc định, sau đó thử tên còn lại giữa python và python3.
     */
    @Override
    public boolean isAvailable() {
        if (super.isAvailable()) return true;
        String other = interpreter.equals("python") ? "python3" : "python";
        ExecutionResult r = probe(Arrays.asList(other, "--version"));
        if (r == null || !r.isSuccess()) return false;
        interpreter = other;
        return true;
    }

    /**
     * Dùng py_compile để kiểm tra cú pháp trước khi chấm từng test.
     */
    @Override
    public CompileResult compile(Path source, Path workDir) throws IOException, InterruptedException {
        return runCompiler(workDir, Arrays.asList(interpreter, "-m", "py_compile", source.toString()));
    }

    /**
     * Tạo lệnh interpreter chạy file Python; chưa áp đặt giới hạn bộ nhớ.
     */
    @Override
    public List<String> buildRunCommand(Path source, Path workDir, int memoryLimitMb) {
        return Arrays.asList(interpreter, source.toString());
    }
}
