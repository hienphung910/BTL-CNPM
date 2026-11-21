package com.ptit.oj.controller.judge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Chạy tiến trình con, cấp stdin và đọc stdout/stderr trên các luồng riêng.
 * Theo dõi thời gian, yêu cầu dừng tiến trình khi quá hạn và thu kết quả thực thi.
 */
public class ProcessRunner {

    // Ngưỡng giữ output khoảng 1 MiB; lần ghi cuối có thể vượt ngưỡng tối đa một chunk.
    private static final int OUTPUT_CAP_BYTES = 1 << 20; // Ngưỡng giữ output trong bộ nhớ.

    /**
     * Khởi chạy lệnh, cấp input và thu output đồng thời; chờ tối đa thời gian quy định.
     * Nếu quá hạn, buộc dừng tiến trình trực tiếp rồi trả kết quả kèm cờ timedOut.
     */
    public ExecutionResult run(List<String> command, Path workDir, String stdin, long timeLimitMs)
            throws IOException, InterruptedException {

        // Bước 1: khởi chạy tiến trình trong thư mục làm việc của bài nộp.
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir.toFile());
        Process process = pb.start();

        long start = System.nanoTime();

        // Bước 2: cấp input và đọc hai luồng output đồng thời để giảm nguy cơ nghẽn bộ đệm.
        StdinFeeder feeder = new StdinFeeder(process.getOutputStream(), stdin);
        StreamCollector out = new StreamCollector(process.getInputStream());
        StreamCollector err = new StreamCollector(process.getErrorStream());

        feeder.start();
        out.start();
        err.start();

        // Bước 3: chờ trong giới hạn cho phép và đo thời gian bằng đồng hồ đơn điệu.
        boolean finished = process.waitFor(timeLimitMs, TimeUnit.MILLISECONDS);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000L;

        // Bước 4: nếu quá hạn, yêu cầu dừng tiến trình trực tiếp và chờ nó kết thúc.
        if (!finished) {
            process.destroyForcibly();
            process.waitFor();
        }

        // Bước 5: chờ các luồng I/O trong thời hạn ngắn rồi đóng gói dữ liệu đã thu được.
        out.join(2000);
        err.join(2000);
        feeder.join(200);

        int exitCode = finished ? process.exitValue() : -1;
        return new ExecutionResult(exitCode, out.getText(), err.getText(), elapsedMs, !finished);
    }

    /**
     * Luồng nền ghi dữ liệu đầu vào cho tiến trình con rồi đóng stdin để báo hết dữ liệu.
     */
    private static class StdinFeeder extends Thread {
        private OutputStream os;
        private String stdin;

        /**
         * Nhận luồng stdin và dữ liệu cần ghi; cấu hình thread nền.
         */
        StdinFeeder(OutputStream os, String stdin) {
            this.os = os;
            this.stdin = stdin;
            setDaemon(true);
        }

        /**
         * Ghi input UTF-8, flush rồi đóng stdin; bỏ qua lỗi khi tiến trình đã kết thúc sớm.
         */
        @Override
        public void run() {
            try {
                if (stdin != null) os.write(stdin.getBytes(StandardCharsets.UTF_8));
                os.flush();
            } catch (IOException ignored) {
                // Bỏ qua lỗi khi tiến trình kết thúc sớm, chưa đọc hết stdin.
            }
            try {
                os.close();         // Đóng stdin để báo hết dữ liệu cho chương trình bài nộp.
            } catch (IOException ignored) {
                // Bỏ qua lỗi đóng luồng khi tiến trình đã kết thúc.
            }
        }
    }

    /**
     * Luồng nền đọc output đến hết luồng, chỉ giữ một lượng dữ liệu giới hạn trong bộ nhớ.
     * Vẫn đọc bỏ phần vượt ngưỡng để tránh làm đầy bộ đệm của tiến trình con.
     */
    private static class StreamCollector extends Thread {
        private InputStream in;
        private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        /**
         * Nhận luồng stdout hoặc stderr cần thu; cấu hình thread nền.
         */
        StreamCollector(InputStream in) {
            this.in = in;
            setDaemon(true);
        }

        /**
         * Đọc từng khối byte đến EOF; chỉ ghi vào buffer khi chưa đạt ngưỡng dung lượng.
         */
        @Override
        public void run() {
            byte[] chunk = new byte[8192];
            int n;
            try {
                while ((n = in.read(chunk)) != -1) {
                    if (buffer.size() < OUTPUT_CAP_BYTES) {
                        buffer.write(chunk, 0, n);
                    }
                }
            } catch (IOException ignored) {
                // Bỏ qua lỗi đọc khi luồng bị đóng do tiến trình đã dừng.
            }
        }

        /**
         * Giải mã phần output đã giữ lại bằng UTF-8.
         */
        String getText() {
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
