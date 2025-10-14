-- Master 초기화 스크립트

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS moko;
USE moko;

-- 사용자 테이블 생성 (Expand 단계)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),  -- 신규 컬럼 (Expand)
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_full_name (full_name),
    INDEX idx_email (email)
);

-- 레플리케이션용 사용자 생성 (mysql_native_password 사용)
CREATE USER IF NOT EXISTS 'replica_user'@'%' IDENTIFIED WITH mysql_native_password BY 'replica_password';
GRANT REPLICATION SLAVE ON *.* TO 'replica_user'@'%';
GRANT SELECT ON *.* TO 'replica_user'@'%';

-- 모든 호스트에서 root 접근 허용
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY '0000';
CREATE USER IF NOT EXISTS 'root'@'127.0.0.1' IDENTIFIED BY '0000';
CREATE USER IF NOT EXISTS 'root'@'::1' IDENTIFIED BY '0000';

GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'127.0.0.1' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'::1' WITH GRANT OPTION;

FLUSH PRIVILEGES;

-- 샘플 데이터 삽입
INSERT IGNORE INTO users (first_name, last_name, full_name, email) VALUES
('김', '철수', '김 철수', 'kim.cs@example.com'),
('이', '영희', '이 영희', 'lee.yh@example.com'),
('박', '민수', '박 민수', 'park.ms@example.com');

-- 레플리케이션 상태 확인용
-- SHOW MASTER STATUS;