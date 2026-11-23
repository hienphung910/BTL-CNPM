package com.ptit.oj.controller.judge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProcessRunner {
    private static final int OUTPUT_CAP_BYTES = 1 << 20;

    public ExecutionResult run(List<String> command, Path workDir, String stdin, long timeLimitMs)
            throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir.toFile());
        Process process = pb.start();

        long start = System.nanoTime();

        StdinFeeder feeder = new StdinFeeder(process.getOutputStream(), stdin);
        StreamCollector out = new StreamCollector(process.getInputStream());
        StreamCollector err = new StreamCollector(process.getErrorStream());

        feeder.start();
        out.start();
        err.start();

        boolean finished = process.waitFor(timeLimitMs, TimeUnit.MILLISECONDS);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000L;

        if (!finished) {
            process.destroyForcibly();
            process.waitFor();
        }

        out.join(2000);
        err.join(2000);
        feeder.join(200);

        int exitCode = finished ? process.exitValue() : -1;
        return new ExecutionResult(exitCode, out.getText(), err.getText(), elapsedMs, !finished);
    }

    private static class StdinFeeder extends Thread {
        private OutputStream os;
        private String stdin;

        StdinFeeder(OutputStream os, String stdin) {
            this.os = os;
            this.stdin = stdin;
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                if (stdin != null) os.write(stdin.getBytes(StandardCharsets.UTF_8));
                os.flush();
            } catch (IOException ignored) {
            }
            try {
                os.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static class StreamCollector extends Thread {
        private InputStream in;
        private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        StreamCollector(InputStream in) {
            this.in = in;
            setDaemon(true);
        }

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
            }
        }

        String getText() {
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
