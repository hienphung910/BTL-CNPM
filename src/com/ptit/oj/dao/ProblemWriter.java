package com.ptit.oj.dao;

import com.ptit.oj.model.ProblemDraft;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ProblemWriter {
    public static final String TEMP_PREFIX = "_tmp_";

    private Path problemsDir;

    public ProblemWriter(Path problemsDir) {
        this.problemsDir = problemsDir;
    }

    public boolean exists(String problemId) {
        return Files.isDirectory(problemsDir.resolve(problemId));
    }

    public Path write(ProblemDraft draft) throws IOException {
        Files.createDirectories(problemsDir);
        Path target = problemsDir.resolve(draft.getId());
        if (Files.exists(target)) {
            throw new IOException("Thư mục bài " + draft.getId() + " đã tồn tại");
        }

        Path temp = problemsDir.resolve(tempName());
        try {
            Files.createDirectory(temp);
            writeText(temp.resolve("problem.properties"), buildProperties(draft));
            writeText(temp.resolve("statement.txt"), draft.getStatement());

            Path testsDir = temp.resolve("tests");
            Files.createDirectory(testsDir);
            for (ProblemDraft.TestDraft t : draft.getTests()) {
                writeText(testsDir.resolve(t.getName() + ".in"), t.getInput());
                writeText(testsDir.resolve(t.getName() + ".out"), t.getOutput());
            }

            Files.move(temp, target);
            return target;
        } catch (IOException | RuntimeException e) {
            deleteRecursively(temp);
            throw e;
        }
    }

    public Path updateProperties(String problemId, Map<String, String> changes) throws IOException {
        Path dir = problemsDir.resolve(problemId);
        Path file = dir.resolve("problem.properties");
        if (!Files.isRegularFile(file)) {
            throw new IOException("Không thấy " + file);
        }

        String text = TextUtils.stripBom(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
        String newline = text.contains("\r\n") ? "\r\n" : "\n";

        List<String> out = new ArrayList<>();
        Set<String> written = new HashSet<>();
        for (String line : text.split("\r?\n")) {
            String key = keyOf(line);
            if (key == null || !changes.containsKey(key)) {
                out.add(line);
            } else if (changes.get(key) != null && written.add(key)) {
                out.add(key + "=" + escapeProperty(changes.get(key)));
            }
        }

        for (Map.Entry<String, String> e : changes.entrySet()) {
            if (e.getValue() != null && !written.contains(e.getKey())) {
                out.add(e.getKey() + "=" + escapeProperty(e.getValue()));
            }
        }

        Path temp = dir.resolve(tempName() + ".properties");
        try {
            writeText(temp, String.join(newline, out) + newline);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | RuntimeException e) {
            Files.deleteIfExists(temp);
            throw e;
        }
        return dir;
    }

    private static String keyOf(String line) {
        String trimmed = line.trim();
        int eq = trimmed.indexOf('=');
        return trimmed.startsWith("#") || eq <= 0 ? null : trimmed.substring(0, eq).trim();
    }

    private static String tempName() {
        return TEMP_PREFIX + UUID.randomUUID().toString().substring(0, 8);
    }

    private String buildProperties(ProblemDraft d) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Sinh tu giao dien quan tri\n");
        sb.append("id=").append(escapeProperty(d.getId())).append('\n');
        sb.append("title=").append(escapeProperty(d.getTitle())).append('\n');
        sb.append("timeLimitMs=").append(d.getTimeLimitMs()).append('\n');
        sb.append("memoryLimitMb=").append(d.getMemoryLimitMb()).append('\n');
        sb.append("comparator=").append(escapeProperty(d.getComparator())).append('\n');
        sb.append("totalPoints=").append(formatPoints(d.getTotalPoints())).append('\n');
        if (d.getTopic() != null && !d.getTopic().isEmpty()) {
            sb.append("topic=").append(escapeProperty(d.getTopic())).append('\n');
        }
        if (d.getDifficulty() > 0) {
            sb.append("difficulty=").append(d.getDifficulty()).append('\n');
        }
        return sb.toString();
    }

    private String formatPoints(double points) {
        if (points == Math.rint(points) && Math.abs(points) < 1e15) return String.valueOf((long) points);
        return String.valueOf(points);
    }

    private String escapeProperty(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '=' || c == ':' || c == '#' || c == '!' || c == '\\') {
                sb.append('\\').append(c);
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c != '\r') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private void writeText(Path file, String content) throws IOException {
        Files.write(file, (content == null ? "" : content).getBytes(StandardCharsets.UTF_8));
    }

    private void deleteRecursively(Path dir) {
        if (dir == null) return;
        deleteRecursively(dir.toFile());
    }

    private void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }

        file.delete();
    }
}
