package com.ptit.oj.controller.judge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class JavaScriptLanguage extends Language {
    @Override public String getName() { return "JavaScript"; }

    @Override public String getFileExtension() { return ".js"; }

    @Override public long getStartupOverheadMs() { return 250; }

    @Override
    protected List<String> getVersionCommand() {
        return Arrays.asList("node", "--version");
    }

    @Override
    public CompileResult compile(Path source, Path workDir) throws IOException, InterruptedException {
        return runCompiler(workDir, Arrays.asList("node", "--check", source.toString()));
    }

    @Override
    public List<String> buildRunCommand(Path source, Path workDir, int memoryLimitMb) {
        return Arrays.asList("node", "--max-old-space-size=" + memoryLimitMb, source.toString());
    }
}
