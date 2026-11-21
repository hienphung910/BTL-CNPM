package com.ptit.oj.dao;

import com.ptit.oj.model.Problem;
import com.ptit.oj.model.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

public class ProblemLoader {
    private Path problemsDir;

    public ProblemLoader(Path problemsDir) {
        this.problemsDir = problemsDir;
    }

    public List<Problem> loadAll() {
        if (!Files.isDirectory(problemsDir)) {
            throw new ProblemLoadException("Không thấy thư mục đề bài: " + problemsDir.toAbsolutePath());
        }
        List<Problem> problems = new ArrayList<>();
        File[] dirs = problemsDir.toFile().listFiles();
        if (dirs == null) {
            throw new ProblemLoadException("Lỗi đọc thư mục đề bài: " + problemsDir.toAbsolutePath());
        }
        for (File dir : dirs) {
            if (!dir.isDirectory()) continue;

            String name = dir.getName();
            if (name.startsWith(ProblemWriter.TEMP_PREFIX) || name.startsWith(".")) continue;
            problems.add(load(dir.toPath()));
        }
        Collections.sort(problems, new Comparator<Problem>() {
            @Override
            public int compare(Problem a, Problem b) {
                return a.getId().compareTo(b.getId());
            }
        });
        return problems;
    }

    public Problem load(Path dir) {
        Path configFile = dir.resolve("problem.properties");
        if (!Files.isRegularFile(configFile)) {
            throw new ProblemLoadException("Thiếu problem.properties trong " + dir);
        }
        Properties config = new Properties();

        try (Reader reader = new StringReader(readText(configFile))) {
            config.load(reader);
        } catch (IOException e) {
            throw new ProblemLoadException("Không đọc được " + configFile, e);
        }

        String id = config.getProperty("id", dir.getFileName().toString()).trim();
        String title = config.getProperty("title", id).trim();
        long timeLimit = parseLong(config.getProperty("timeLimitMs"), 2000);
        int memoryLimit = (int) parseLong(config.getProperty("memoryLimitMb"), 256);
        String comparator = config.getProperty("comparator", "token").trim();
        double totalPoints = parseDouble(config.getProperty("totalPoints"), 100);
        String topic = config.getProperty("topic", "").trim();
        int difficulty = (int) parseLong(config.getProperty("difficulty"), 0);
        if (difficulty < 0 || difficulty > 3) difficulty = 0;
        long order = parseLong(config.getProperty("order"), 0);
        if (order < 0 || order > 9999) order = 0;
        String statement = readTextIfExists(dir.resolve("statement.txt"));

        Problem problem = new Problem(id, title, statement, timeLimit, memoryLimit, comparator,
                topic, difficulty, (int) order);

        List<Path> inputs = listInputs(dir.resolve("tests"));
        if (inputs.isEmpty()) {
            throw new ProblemLoadException("Bài " + id + " không có test nào trong tests/");
        }
        double pointsPerTest = totalPoints / inputs.size();

        for (Path in : inputs) {
            String base = in.getFileName().toString().replaceAll("\\.in$", "");
            Path out = in.getParent().resolve(base + ".out");
            if (!Files.isRegularFile(out)) {
                throw new ProblemLoadException("Thiếu file đáp án cho test " + in.getFileName());
            }
            boolean sample = base.toLowerCase().startsWith("sample");
            problem.addTestCase(new TestCase(base, readText(in), readText(out), pointsPerTest, sample));
        }
        return problem;
    }

    private List<Path> listInputs(Path testsDir) {
        if (!Files.isDirectory(testsDir)) return Collections.emptyList();
        List<Path> inputs = new ArrayList<>();
        File[] files = testsDir.toFile().listFiles();
        if (files == null) {
            throw new ProblemLoadException("Lỗi đọc thư mục test " + testsDir);
        }
        for (File f : files) {
            if (f.getName().toLowerCase().endsWith(".in")) inputs.add(f.toPath());
        }

        Collections.sort(inputs, new Comparator<Path>() {
            @Override
            public int compare(Path a, Path b) {
                String nameA = a.getFileName().toString().toLowerCase();
                String nameB = b.getFileName().toString().toLowerCase();
                boolean sampleA = nameA.startsWith("sample");
                boolean sampleB = nameB.startsWith("sample");
                if (sampleA && !sampleB) return -1;
                if (!sampleA && sampleB) return 1;
                return nameA.compareTo(nameB);
            }
        });
        return inputs;
    }

    private String readText(Path file) {
        try {
            return TextUtils.stripBom(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ProblemLoadException("Không đọc được file " + file, e);
        }
    }

    private String readTextIfExists(Path file) {
        return Files.isRegularFile(file) ? readText(file) : "";
    }

    private long parseLong(String value, long fallback) {
        try {
            return value == null ? fallback : Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private double parseDouble(String value, double fallback) {
        try {
            return value == null ? fallback : Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
