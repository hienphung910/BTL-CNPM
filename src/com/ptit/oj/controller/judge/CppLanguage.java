package com.ptit.oj.controller.judge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Cài đặt cách chấm C++: biên dịch bằng g++ theo chuẩn C++17 và chạy file thực thi.
 * Lớp này chưa áp đặt giới hạn bộ nhớ của tiến trình.
 */
public class CppLanguage extends Language {

    /**
     * Trả tên ngôn ngữ để hiển thị và tra cứu.
     */
    @Override public String getName() { return "C++"; }

    /**
     * Trả phần mở rộng của file mã nguồn tương ứng.
     */
    @Override public String getFileExtension() { return ".cpp"; }

    /**
     * Tạo lệnh kiểm tra phiên bản toolchain của ngôn ngữ.
     */
    @Override
    protected List<String> getVersionCommand() {
        return Arrays.asList("g++", "--version");
    }

    /**
     * Gọi g++ với tối ưu -O2 và chuẩn C++17 để tạo file thực thi.
     */
    @Override
    public CompileResult compile(Path source, Path workDir) throws IOException, InterruptedException {
        return runCompiler(workDir, Arrays.asList("g++", "-O2", "-std=c++17",
                "-o", executablePath(source, workDir).toString(), source.toString()));
    }

    /**
     * Chạy trực tiếp file đã biên dịch; tham số memoryLimitMb chưa được áp dụng tại đây.
     */
    @Override
    public List<String> buildRunCommand(Path source, Path workDir, int memoryLimitMb) {
        // Chưa giới hạn bộ nhớ tiến trình; Judge nhận diện MLE qua thông báo lỗi runtime.
        return Collections.singletonList(executablePath(source, workDir).toString());
    }
}
