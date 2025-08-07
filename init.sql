-- MySQL 초기화 스크립트
-- 데이터베이스 및 사용자 설정

-- 데이터베이스 생성 (이미 docker-compose에서 생성되지만 명시적으로 작성)
CREATE DATABASE IF NOT EXISTS board_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 권한 설정
GRANT ALL PRIVILEGES ON board_db.* TO 'board_user'@'%';
FLUSH PRIVILEGES;

-- 기본 데이터 삽입 (선택사항)
USE board_db;

-- 관리자 계정 생성을 위한 테이블이 없으므로 애플리케이션 시작 시 자동 생성됨
-- 필요시 여기에 초기 데이터 삽입 스크립트 추가 가능
