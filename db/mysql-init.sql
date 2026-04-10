CREATE DATABASE IF NOT EXISTS codeshow
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE codeshow;

CREATE TABLE IF NOT EXISTS language_mst (
    language_id INT NOT NULL AUTO_INCREMENT,
    language_name VARCHAR(10) NOT NULL,
    CONSTRAINT pk_language_mst PRIMARY KEY (language_id)
);

CREATE TABLE IF NOT EXISTS user_mst (
    user_id INT NOT NULL AUTO_INCREMENT,
    login_id VARCHAR(25) NOT NULL,
    password VARCHAR(255) NOT NULL,
    user_name VARCHAR(10) NOT NULL,
    create_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT pk_user_mst PRIMARY KEY (user_id),
    CONSTRAINT uk_user_mst_login_id UNIQUE (login_id)
);

CREATE TABLE IF NOT EXISTS animation_mst (
    animation_code INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    language_id INT NOT NULL,
    animation_name VARCHAR(25) NOT NULL,
    original_code TEXT NOT NULL,
    json_data JSON NOT NULL,
    create_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT pk_animation_mst PRIMARY KEY (animation_code),
    CONSTRAINT fk_animation_mst_user FOREIGN KEY (user_id) REFERENCES user_mst (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_animation_mst_language FOREIGN KEY (language_id) REFERENCES language_mst (language_id)
);

INSERT IGNORE INTO language_mst (language_id, language_name) VALUES
    (1, 'Java'),
    (2, 'Python'),
    (3, 'JavaScript'),
    (4, 'C++');
