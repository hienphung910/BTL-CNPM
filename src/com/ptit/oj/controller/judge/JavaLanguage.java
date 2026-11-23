package com.ptit.oj.controller.judge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaLanguage extends Language {
    private static final Pattern CLASS_NAME = Pattern.compile(
            "(?m)^\\s*(?:public\\s+)?(?:final\\s+|abstract\\s+)?class\\s+([A-Za-z_$][\\w$]*)");

    @Override public String getName() { return "Java"; }

    @Override public String getFileExtension() { return ".java"; }

    @Override public long getStartupOverheadMs() { return 400; }

    @Override
    protected List<String> getVersionCommand() {
        return Arrays.asList("javac", "-version");
    }

    @Override
    public String defaultFileName(String sourceCode) {
        Matcher m = CLASS_NAME.matcher(sourceCode == null ? "" : sourceCode);
        return (m.find() ? m.group(1) : "Main") + getFileExtension();
    }

    @Override
    public CompileResult compile(Path source, Path workDir) throws IOException, InterruptedException {
        return runCompiler(workDir,
                Arrays.asList("javac", "-encoding", "UTF-8", "-d", workDir.toString(), source.toString()));
    }

    @Override
    public List<String> buildRunCommand(Path source, Path workDir, int memoryLimitMb) {
        return Arrays.asList("java",
                "-Xmx" + memoryLimitMb + "m",
                "-Dfile.encoding=UTF-8",
                "-cp", workDir.toString(),
                baseName(source));
    }
}
