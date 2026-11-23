package com.ptit.oj.controller;

import com.ptit.oj.controller.judge.ComparatorFactory;
import com.ptit.oj.dao.ProblemLoader;
import com.ptit.oj.dao.ProblemWriter;
import com.ptit.oj.dao.Repository;
import com.ptit.oj.model.Problem;
import com.ptit.oj.model.ProblemDraft;
import com.ptit.oj.model.Topic;
import com.ptit.oj.model.User;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ProblemAdminService {
    private static final String ID_PATTERN = "[A-Z0-9_-]{1,32}";

    private static final String TEST_NAME_PATTERN = "[A-Za-z0-9_-]{1,64}";

    private static final long MIN_TIME_LIMIT_MS = 100;
    private static final long MAX_TIME_LIMIT_MS = 60_000;
    private static final int MIN_MEMORY_MB = 8;
    private static final int MAX_MEMORY_MB = 2048;
    private static final double MAX_TOTAL_POINTS = 100_000;
    private static final int MAX_TESTS = 200;
    private static final int MAX_TITLE = 200;
    private static final int MAX_STATEMENT = 200_000;
    private static final int MAX_TEST_CHARS = 1_000_000;

    private Repository<Problem> problems;
    private ProblemWriter writer;
    private ProblemLoader loader;

    private List<Topic> topics;

    public ProblemAdminService(Repository<Problem> problems, Path problemsDir, List<Topic> topics) {
        this.problems = problems;
        this.writer = new ProblemWriter(problemsDir);
        this.loader = new ProblemLoader(problemsDir);
        this.topics = topics;
    }

    private boolean topicExists(String id) {
        for (Topic t : topics) {
            if (t.getId().equals(id)) return true;
        }
        return false;
    }

    public static class ProblemAdminException extends RuntimeException {
        public ProblemAdminException(String message) {
            super(message);
        }
    }

    public Problem createProblem(User actor, ProblemDraft draft) {
        if (actor == null || !actor.canCreateProblem()) {
            throw new ProblemAdminException("Chỉ tài khoản quản trị mới được tạo bài tập");
        }
        validate(draft);

        Path dir;
        try {
            dir = writer.write(draft);
        } catch (IOException e) {
            throw new ProblemAdminException("Không ghi được bài tập ra ổ đĩa: " + e.getMessage());
        }

        Problem problem = loader.load(dir);
        problems.save(problem);
        return problem;
    }

    public Problem updateProblemMeta(User actor, String rawId, String rawTopic, int difficulty) {
        if (actor == null || !actor.canCreateProblem()) {
            throw new ProblemAdminException("Chỉ tài khoản quản trị mới được sửa bài tập");
        }
        String id = normalizeId(rawId);
        Problem current = problems.findById(id);
        if (current == null) {
            throw new ProblemAdminException("Không có bài " + id);
        }
        String topic = validateTopic(rawTopic);
        validateDifficulty(difficulty);

        Map<String, String> changes = new LinkedHashMap<>();
        changes.put("topic", topic.isEmpty() ? null : topic);
        changes.put("difficulty", difficulty > 0 ? String.valueOf(difficulty) : null);
        if (!topic.equals(current.getTopic())) {
            changes.put("order", null);
        }
        try {
            Problem problem = loader.load(writer.updateProperties(id, changes));
            problems.save(problem);
            return problem;
        } catch (IOException e) {
            throw new ProblemAdminException("Không ghi được bài tập ra ổ đĩa: " + e.getMessage());
        }
    }

    private void validate(ProblemDraft d) {
        String id = normalizeId(d.getId());
        d.setId(id);

        if (problems.findById(id) != null || writer.exists(id)) {
            throw new ProblemAdminException("Mã bài " + id + " đã tồn tại");
        }

        d.setTitle(requireText(d.getTitle(), "Tên bài", MAX_TITLE));
        d.setStatement(requireText(d.getStatement(), "Nội dung đề", MAX_STATEMENT));

        if (d.getTimeLimitMs() < MIN_TIME_LIMIT_MS || d.getTimeLimitMs() > MAX_TIME_LIMIT_MS) {
            throw new ProblemAdminException("Giới hạn thời gian phải từ " + MIN_TIME_LIMIT_MS + " đến " + MAX_TIME_LIMIT_MS + " ms");
        }
        if (d.getMemoryLimitMb() < MIN_MEMORY_MB || d.getMemoryLimitMb() > MAX_MEMORY_MB) {
            throw new ProblemAdminException("Giới hạn bộ nhớ phải từ " + MIN_MEMORY_MB + " đến " + MAX_MEMORY_MB + " MB");
        }
        if (!(d.getTotalPoints() > 0) || d.getTotalPoints() > MAX_TOTAL_POINTS) {
            throw new ProblemAdminException("Tổng điểm phải lớn hơn 0 và tối đa " + (long) MAX_TOTAL_POINTS);
        }

        String comparator = d.getComparator() == null ? "token" : d.getComparator().trim();
        try {
            ComparatorFactory.create(comparator);
        } catch (IllegalArgumentException e) {
            throw new ProblemAdminException(e.getMessage());
        }
        d.setComparator(comparator);

        d.setTopic(validateTopic(d.getTopic()));
        validateDifficulty(d.getDifficulty());

        if (d.getTests().isEmpty()) {
            throw new ProblemAdminException("Bài tập phải có ít nhất một test");
        }
        if (d.getTests().size() > MAX_TESTS) {
            throw new ProblemAdminException("Tối đa " + MAX_TESTS + " test cho một bài");
        }

        Set<String> seen = new LinkedHashSet<>();
        for (ProblemDraft.TestDraft t : d.getTests()) {
            validateTest(t, seen);
        }
    }

    private String validateTopic(String rawTopic) {
        String topic = rawTopic == null ? "" : rawTopic.trim();
        if (!topic.isEmpty() && !topicExists(topic)) {
            throw new ProblemAdminException("Chủ đề \"" + topic + "\" không có trong lộ trình (data/topics.txt)");
        }
        return topic;
    }

    private void validateDifficulty(int difficulty) {
        if (difficulty < 0 || difficulty > 3) {
            throw new ProblemAdminException("Độ khó phải là 1 (Dễ), 2 (Vừa), 3 (Khó) hoặc 0 (chưa đánh giá)");
        }
    }

    private void validateTest(ProblemDraft.TestDraft t, Set<String> seen) {
        String name = t.getName() == null ? "" : t.getName().trim();
        if (name.isEmpty()) {
            throw new ProblemAdminException("Tên test không được để trống");
        }

        if (name.contains("..") || name.contains("/") || name.contains("\\")) {
            throw new ProblemAdminException("Tên test \"" + name + "\" chứa ký tự đường dẫn");
        }
        if (!name.matches(TEST_NAME_PATTERN)) {
            throw new ProblemAdminException("Tên test \"" + name + "\" chỉ được gồm chữ, số, gạch dưới hoặc gạch ngang");
        }

        if (!seen.add(name.toLowerCase(Locale.ROOT))) {
            throw new ProblemAdminException("Tên test \"" + name + "\" bị trùng");
        }

        boolean looksSample = name.toLowerCase(Locale.ROOT).startsWith("sample");
        if (!t.isSample() && looksSample) {
            throw new ProblemAdminException("Test ẩn không được đặt tên bắt đầu bằng \"sample\" (tên này dành cho test ví dụ)");
        }
        if (t.isSample() && !looksSample) {
            throw new ProblemAdminException("Test ví dụ phải có tên bắt đầu bằng \"sample\", ví dụ sample01");
        }

        requireText(t.getInput(), "Input của test " + name, MAX_TEST_CHARS);
        requireText(t.getOutput(), "Output của test " + name, MAX_TEST_CHARS);
    }

    private String normalizeId(String rawId) {
        String id = rawId == null ? "" : rawId.trim().toUpperCase(Locale.ROOT);
        if (id.isEmpty()) {
            throw new ProblemAdminException("Thiếu mã bài");
        }
        if (id.contains("..") || id.contains("/") || id.contains("\\")) {
            throw new ProblemAdminException("Mã bài chứa ký tự đường dẫn không hợp lệ");
        }
        if (!id.matches(ID_PATTERN)) {
            throw new ProblemAdminException("Mã bài chỉ được gồm chữ in hoa, số, gạch dưới hoặc gạch ngang (tối đa 32 ký tự)");
        }
        return id;
    }

    private String requireText(String value, String label, int max) {
        String v = value == null ? "" : value;
        if (v.trim().isEmpty()) {
            throw new ProblemAdminException(label + " không được để trống");
        }
        if (v.length() > max) {
            throw new ProblemAdminException(label + " quá dài (tối đa " + max + " ký tự)");
        }
        return v;
    }
}
