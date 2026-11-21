package com.ptit.oj.controller.judge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Cài đặt cách chấm JavaScript qua Node.js: kiểm tra cú pháp và chạy mã nguồn.
 * Tham số --max-old-space-size giới hạn vùng old space của heap, không phải toàn bộ bộ nhớ.
 */
public class JavaScriptLanguage extends Language {

    /**
     * Trả tên ngôn ngữ để hiển thị và tra cứu.
     */
    @Override public String getName() { return "JavaScript"; }

    /**
     * Trả phần mở rộng của file mã nguồn tương ứng.
     */
    @Override public String getFileExtension() { return ".js"; }

    /**
     * Trả số mili giây bù thêm cho thời gian khởi động runtime.
     */
    @Override public long getStartupOverheadMs() { return 250; }

    /**
     * Tạo lệnh kiểm tra phiên bản toolchain của ngôn ngữ.
     */
    @Override
    protected List<String> getVersionCommand() {
        return Arrays.asList("node", "--version");
    }

    /**
     * Dùng node --check để kiểm tra cú pháp mà chưa thực thi chương trình.
     */
    @Override
    public CompileResult compile(Path source, Path workDir) throws IOException, InterruptedException {
        return runCompiler(workDir, Arrays.asList("node", "--check", source.toString()));
    }

    /**
     * Tạo lệnh Node.js và cấu hình giới hạn old space theo memoryLimitMb.
     */
    @Override
    public List<String> buildRunCommand(Path source, Path workDir, int memoryLimitMb) {
        return Arrays.asList("node", "--max-old-space-size=" + memoryLimitMb, source.toString());
    }
}
