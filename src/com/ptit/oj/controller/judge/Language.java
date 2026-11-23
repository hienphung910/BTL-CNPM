package com.ptit.oj.controller.judge;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public abstract class Language {
    private static final long COMPILE_TIME_LIMIT_MS = 20_000;

    protected ProcessRunner runner = new ProcessRunner();
    private Boolean availableCache;

    public abstract String getName();

    public abstract String getFileExtension();

    protected abstract List<String> getVersionCommand();

    public abstract CompileResult compile(Path source, Path workDir) throws IOException, InterruptedException;

    public abstract List<String> buildRunCommand(Path source, Path workDir, int memoryLimitMb);

    public long getStartupOverheadMs() { return 0; }

    public String defaultFileName(String sourceCode) {
        return "solution" + getFileExtension();
    }

    public boolean isAvailable() {
        if (availableCache == null) {
            ExecutionResult r = probe(getVersionCommand());
            availableCache = r != null && !r.isTimedOut();
        }
        return availableCache;
    }

    protected ExecutionResult probe(List<String> command) {
        try {
            return runner.run(command, Paths.get(System.getProperty("user.dir")), "", 10_000);
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    protected CompileResult runCompiler(Path workDir, List<String> command) throws IOException, InterruptedException {
        ExecutionResult r = runner.run(command, workDir, "", COMPILE_TIME_LIMIT_MS);
        if (r.isTimedOut()) return CompileResult.fail("Biên dịch quá lâu (> 20s)");
        if (r.getExitCode() != 0) {
            String message = r.getStderr().isEmpty() ? r.getStdout() : r.getStderr();
            return CompileResult.fail(message.trim());
        }
        return CompileResult.ok();
    }

    protected static Path executablePath(Path source, Path workDir) {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        return workDir.resolve(baseName(source) + (windows ? ".exe" : ".bin"));
    }

    protected static String baseName(Path source) {
        String name = source.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }

    @Override
    public String toString() { return getName(); }
}
