package com.ptit.oj.controller;

import com.ptit.oj.controller.judge.Judge;
import com.ptit.oj.controller.judge.Language;
import com.ptit.oj.controller.judge.LanguageRegistry;
import com.ptit.oj.dao.InMemoryRepository;
import com.ptit.oj.dao.InMemorySubmissionRepository;
import com.ptit.oj.dao.InMemoryUserRepository;
import com.ptit.oj.dao.MySqlDatabaseManager;
import com.ptit.oj.dao.MySqlSchemaInitializer;
import com.ptit.oj.dao.MySqlSubmissionRepository;
import com.ptit.oj.dao.MySqlUserRepository;
import com.ptit.oj.dao.ProblemLoader;
import com.ptit.oj.dao.Repository;
import com.ptit.oj.dao.SubmissionRepository;
import com.ptit.oj.dao.TextUtils;
import com.ptit.oj.dao.TopicLoader;
import com.ptit.oj.dao.UserRepository;
import com.ptit.oj.model.JudgeResult;
import com.ptit.oj.model.Problem;
import com.ptit.oj.model.ProblemDraft;
import com.ptit.oj.model.Submission;
import com.ptit.oj.model.Topic;
import com.ptit.oj.model.User;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JudgeService {
    private Path dataDir;
    private Repository<Problem> problems;
    private UserRepository users;
    private SubmissionRepository submissions;
    private LanguageRegistry languages;
    private Judge judge = new Judge();

    private static final int MAX_SOURCE_CHARS = 512 * 1024;

    private AuthService auth;
    private ProblemAdminService problemAdmin;
    private ScoreboardService scoreboard;

    private List<Topic> topics = new ArrayList<>();

    public JudgeService(Path dataDir, Repository<Problem> problems, UserRepository users,
                        SubmissionRepository submissions, LanguageRegistry languages) {
        this.dataDir = dataDir;
        this.problems = problems;
        this.users = users;
        this.submissions = submissions;
        this.languages = languages;
        this.auth = new AuthService(users, new PasswordService(), new SessionService());
        this.problemAdmin = new ProblemAdminService(problems, dataDir.resolve("problems"), topics);
        this.scoreboard = new ScoreboardService(submissions, problems);
    }

    public static JudgeService mysql(Path dataDir, MySqlDatabaseManager database) {
        new MySqlSchemaInitializer(database).initialize();

        Repository<Problem> problems = new InMemoryRepository<>();
        LanguageRegistry languages = new LanguageRegistry();
        MySqlUserRepository users = new MySqlUserRepository(database);
        MySqlSubmissionRepository submissions =
                new MySqlSubmissionRepository(database, problems, users, languages);
        return new JudgeService(dataDir, problems, users, submissions, languages);
    }

    public static JudgeService inMemory(Path dataDir) {
        InMemoryUserRepository users = new InMemoryUserRepository();
        InMemorySubmissionRepository submissions = new InMemorySubmissionRepository();
        users.setSubmissionRepository(submissions);
        return new JudgeService(dataDir, new InMemoryRepository<>(), users, submissions, new LanguageRegistry());
    }

    public void loadData() {
        topics.clear();
        topics.addAll(new TopicLoader(dataDir.resolve("topics.txt")).loadAll());
        for (Problem p : new ProblemLoader(dataDir.resolve("problems")).loadAll()) {
            problems.save(p);
            if (!p.getTopic().isEmpty() && findTopic(p.getTopic()) == null) {
                System.out.println("Cảnh báo: bài " + p.getId() + " gắn chủ đề \"" + p.getTopic()
                        + "\" không có trong data/topics.txt - sẽ hiện ở nhóm \"Bài khác\".");
            }
        }
    }

    public AuthService getAuth() { return auth; }

    public List<User> getUsers() { return users.findAll(); }

    public List<Problem> getProblems() { return problems.findAll(); }

    public List<Topic> getTopics() { return Collections.unmodifiableList(topics); }

    public Topic findTopic(String id) {
        for (Topic t : topics) {
            if (t.getId().equals(id)) return t;
        }
        return null;
    }

    public Problem findProblem(String id) {
        if (id == null) return null;
        return problems.findById(id.trim().toUpperCase());
    }

    public Problem createProblem(User actor, ProblemDraft draft) {
        return problemAdmin.createProblem(actor, draft);
    }

    public Problem updateProblemMeta(User actor, String problemId, String topic, int difficulty) {
        return problemAdmin.updateProblemMeta(actor, problemId, topic, difficulty);
    }

    public List<Language> getLanguages() { return languages.all(); }

    public Language findLanguage(String name) { return languages.byName(name); }

    public JudgeResult submitCode(Problem problem, User author, Language language, String code) {
        if (!language.isAvailable()) {
            throw new IllegalArgumentException("Máy này chưa cài toolchain cho " + language.getName());
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nguồn rỗng");
        }
        if (code.length() > MAX_SOURCE_CHARS) {
            throw new IllegalArgumentException("Mã nguồn lớn hơn 512 KB");
        }
        String source = TextUtils.stripBom(code);
        Submission submission = new Submission(problem, author, language,
                language.defaultFileName(source), source);
        JudgeResult result = judge.judge(submission);

        submissions.saveWithResults(submission);
        return result;
    }

    public boolean canSeeEveryone(User user) {
        return User.ROLE_ADMIN.equals(user.getRoleCode()) || User.ROLE_TEACHER.equals(user.getRoleCode());
    }

    public List<Submission> getSubmissionsFor(User viewer) {
        if (canSeeEveryone(viewer)) {
            return getSubmissionsNewestFirst();
        }
        return getSubmissionsOf(viewer);
    }

    public Submission getSubmissionDetail(User viewer, String id) {
        Submission s = findSubmission(id);
        if (s == null) {
            throw new IllegalArgumentException("Không có mã nộp " + id);
        }
        if (!canSeeEveryone(viewer) && !s.getAuthor().getId().equals(viewer.getId())) {
            throw new IllegalArgumentException("Bạn chỉ được xem bài nộp của chính mình");
        }
        return s;
    }

    public List<Submission> getSubmissionsNewestFirst() { return submissions.findAllNewestFirst(); }

    public List<Submission> getSubmissionsOf(User user) {
        if (user == null) return new ArrayList<>();
        return submissions.findByUserId(user.getId());
    }

    public Submission findSubmission(String id) {
        if (id == null) return null;
        return submissions.findById(id.trim().toUpperCase());
    }

    public Map<String, Integer> getSolvedCountByUser() { return users.solvedCountByUser(); }

    public List<ScoreboardService.Row> buildScoreboard() { return scoreboard.buildScoreboard(); }
}
