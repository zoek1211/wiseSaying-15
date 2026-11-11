-- 명언 앱 ⑧ setup script: creates the dev and test databases and the wise_saying table in each.
-- Run once:  mysql -uroot < sql/schema.sql   (add -p if your MySQL user has a password)
-- The app itself never creates databases or tables.

CREATE DATABASE IF NOT EXISTS wise_saying_dev
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wise_saying_dev.wise_saying
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    content     TEXT         NOT NULL,
    author      VARCHAR(100) NOT NULL,
    create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    modify_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS wise_saying_test
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wise_saying_test.wise_saying
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    content     TEXT         NOT NULL,
    author      VARCHAR(100) NOT NULL,
    create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    modify_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
