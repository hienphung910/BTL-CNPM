package com.ptit.oj.controller.judge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class PythonLanguage extends Language {
    private String interpreter = System.getProperty("os.name").toLowerCase().contains("win") ? "python" : "python3";

    @Override public String getName() { return "Python"; }

    @Override public String getFileExtension() { return ".py"; }

    @Override public long getStartupOverheadMs() { return 300; }

    @Override
    protected List<String> getVersionCommand() {
        return Arrays.asList(interpreter, "--version");
    }

    @Override
    public boolean isAvailable() {
        if (super.isAvailable()) return true;
        String other = interpreter.equals("python") ? "python3" : "python";
        ExecutionResult r = probe(Arrays.asList(other, "--version"));
        if (r == null || !r.isSuccess()) return false;
        interpreter = other;
        return true;
    }

    @Override
    public CompileResult compile(Path source, Path workDir) throws IOException, InterruptedException {
        return runCompiler(workDir, Arrays.asList(interpreter, "-m", "py_compile", source.toString()));
    }

    @Override
    public List<String> buildRunCommand(Path source, Path workDir, int memoryLimitMb) {
        return Arrays.asList(interpreter, source.toString());
    }
}
