package com.ptit.oj.controller.judge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cài đặt cách chấm Java: tìm tên class, biên dịch bằng javac và chạy bằng JVM.
 * Tham số -Xmx giới hạn heap của JVM, không phải toàn bộ bộ nhớ tiến trình.
 */
public class JavaLanguage extends Language {

    // Mẫu tìm tên class ở đầu dòng để đặt tên file Java.
    private static final Pattern CLASS_NAME = Pattern.compile(
            "(?m)^\\s*(?:public\\s+)?(?:final\\s+|abstract\\s+)?class\\s+([A-Za-z_$][\\w$]*)");

    /**
     * Trả tên ngôn ngữ để hiển thị và tra cứu.
     */
    @Override public String getName() { return "Java"; }

    /**
     * Trả phần mở rộng của file mã nguồn tương ứng.
     */
    @Override public String getFileExtension() { return ".java"; }

    /**
     * Trả số mili giây bù thêm cho thời gian khởi động runtime.
     */
    @Override public long getStartupOverheadMs() { return 400; }    // Bù thời gian khởi động JVM.

    /**
     * Tạo lệnh kiểm tra phiên bản toolchain của ngôn ngữ.
     */
    @Override
    protected List<String> getVersionCommand() {
        return Arrays.asList("javac", "-version");
    }

    /**
     * Lấy tên class đầu tiên khớp mẫu ở đầu dòng để đặt tên file; mặc định dùng Main.java.
     * Đây là nhận diện bằng biểu thức chính quy, không phải bộ phân tích cú pháp Java đầy đủ.
     */
    @Override
    public String defaultFileName(String sourceCode) {
        Matcher m = CLASS_NAME.matcher(sourceCode == null ? "" : sourceCode);
        return (m.find() ? m.group(1) : "Main") + getFileExtension();
    }

    /**
     * Gọi javac với mã hóa UTF-8 và đặt các file .class trong thư mục làm việc.
     */
    @Override
    public CompileResult compile(Path source, Path workDir) throws IOException, InterruptedException {
        return runCompiler(workDir,
                Arrays.asList("javac", "-encoding", "UTF-8", "-d", workDir.toString(), source.toString()));
    }

    /**
     * Tạo lệnh JVM với giới hạn heap, mã hóa UTF-8, classpath và tên class cần chạy.
     */
    @Override
    public List<String> buildRunCommand(Path source, Path workDir, int memoryLimitMb) {
        return Arrays.asList("java",
                "-Xmx" + memoryLimitMb + "m",
                "-Dfile.encoding=UTF-8",
                "-cp", workDir.toString(),
                baseName(source));
    }
}
