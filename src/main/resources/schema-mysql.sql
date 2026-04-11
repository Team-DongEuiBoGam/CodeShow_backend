-- 언어 목록을 저장하는 테이블
CREATE TABLE IF NOT EXISTS language_mst (
    language_id INT NOT NULL AUTO_INCREMENT,
    language_name VARCHAR(10) NOT NULL,
    CONSTRAINT pk_language_mst PRIMARY KEY (language_id)
);

-- 회원가입한 사용자 정보를 저장하는 테이블
CREATE TABLE IF NOT EXISTS user_mst (
    user_id INT NOT NULL AUTO_INCREMENT,
    login_id VARCHAR(25) NOT NULL,
    password VARCHAR(255) NOT NULL,
    user_name VARCHAR(10) NOT NULL,
    create_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT pk_user_mst PRIMARY KEY (user_id),
    CONSTRAINT uk_user_mst_login_id UNIQUE (login_id)
);

-- 애니메이션 기본 정보와 JSON 데이터를 함께 저장하는 테이블
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

-- 앱 시작 시 기본 언어 목록을 미리 넣어둔다.
INSERT IGNORE INTO language_mst (language_id, language_name) VALUES
    (1, 'Java'),
    (2, 'Python'),
    (3, 'C');
