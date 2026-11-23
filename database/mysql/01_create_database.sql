CREATE DATABASE IF NOT EXISTS online_judge
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'oj_app'@'localhost'
    IDENTIFIED BY 'THAY_MAT_KHAU_NAY';

ALTER USER 'oj_app'@'localhost'
    IDENTIFIED BY 'THAY_MAT_KHAU_NAY';

GRANT SELECT, INSERT, UPDATE, DELETE,
      CREATE, ALTER, INDEX, REFERENCES
    ON online_judge.*
    TO 'oj_app'@'localhost';

GRANT CREATE VIEW, SHOW VIEW ON online_judge.* TO 'oj_app'@'localhost';

FLUSH PRIVILEGES;
