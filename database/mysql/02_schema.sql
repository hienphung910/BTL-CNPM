SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS users (
    id                  VARCHAR(64)  NOT NULL,
    username            VARCHAR(32)  NOT NULL,
    full_name           VARCHAR(150) NOT NULL,
    role                VARCHAR(20)  NOT NULL,
    student_code        VARCHAR(30)      NULL,
    class_name          VARCHAR(50)      NULL,
    department          VARCHAR(150)     NULL,
    password_hash       VARCHAR(255) NOT NULL,
    password_salt       VARCHAR(255) NOT NULL,
    password_iterations INT UNSIGNED NOT NULL,
    created_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_username (username),
    CONSTRAINT chk_users_role CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN'))
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS submissions (
    id             VARCHAR(64)         NOT NULL,
    user_id        VARCHAR(64)         NOT NULL,
    problem_id     VARCHAR(30)         NOT NULL,
    problem_title  VARCHAR(255)        NOT NULL,
    language       VARCHAR(40)         NOT NULL,
    file_name      VARCHAR(255)        NOT NULL,
    source_code    LONGTEXT            NOT NULL,
    submitted_at   DATETIME(6)         NOT NULL,
    verdict        VARCHAR(20)         NOT NULL,
    score          DECIMAL(12,4)       NOT NULL DEFAULT 0,
    max_points     DECIMAL(12,4)       NOT NULL DEFAULT 0,
    max_runtime_ms BIGINT UNSIGNED     NOT NULL DEFAULT 0,
    judge_time_ms  BIGINT UNSIGNED     NOT NULL DEFAULT 0,
    global_message TEXT                NOT NULL,
    PRIMARY KEY (id),
    KEY idx_submissions_user_time    (user_id, submitted_at),
    KEY idx_submissions_user_solved  (user_id, verdict, problem_id),
    KEY idx_submissions_problem      (problem_id),
    KEY idx_submissions_verdict      (verdict),
    KEY idx_submissions_time         (submitted_at),
    CONSTRAINT fk_submissions_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS test_case_results (
    submission_id VARCHAR(64)     NOT NULL,
    test_case_id  VARCHAR(100)    NOT NULL,
    ordinal       INT UNSIGNED    NOT NULL,
    sample        BOOLEAN         NOT NULL DEFAULT FALSE,
    verdict       VARCHAR(20)     NOT NULL,
    runtime_ms    BIGINT UNSIGNED NOT NULL DEFAULT 0,
    earned_points DECIMAL(12,4)   NOT NULL DEFAULT 0,
    message       TEXT            NOT NULL,
    PRIMARY KEY (submission_id, test_case_id),
    KEY idx_test_results_order (submission_id, ordinal),
    CONSTRAINT fk_test_results_submission
        FOREIGN KEY (submission_id) REFERENCES submissions (id)
        ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

SET @user_stats_exists = (
    SELECT COUNT(*) FROM information_schema.views
     WHERE table_schema = DATABASE() AND table_name = 'user_stats');

SET @create_user_stats = IF(@user_stats_exists > 0,
    'DO 0',
    'CREATE VIEW user_stats AS
     SELECT u.id       AS user_id,
            u.username AS username,
            (SELECT COUNT(DISTINCT s.problem_id)
               FROM submissions s
              WHERE s.user_id = u.id
                AND s.verdict = ''AC'')  AS solved_count,
            (SELECT COUNT(*)
               FROM submissions s
              WHERE s.user_id = u.id)    AS submission_count
       FROM users u');

PREPARE stmt FROM @create_user_stats;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
