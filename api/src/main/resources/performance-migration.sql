-- performance 모듈 스키마 보정용 수동 패치 SQL
-- 기존 DB에 schema.sql 변경 사항이 반영되지 않은 경우 실행한다.

-- 동료 평가 확장 컬럼 추가
ALTER TABLE peer_review
    ADD COLUMN IF NOT EXISTS culture_contribution INT NULL AFTER team_contribution;

-- 팀 평가 확장 컬럼 추가
ALTER TABLE team_evaluation
    ADD COLUMN IF NOT EXISTS performance_score INT NULL AFTER solving_eval,
    ADD COLUMN IF NOT EXISTS performance_comment TEXT NULL AFTER performance_score,
    ADD COLUMN IF NOT EXISTS attitude_score INT NULL AFTER performance_comment,
    ADD COLUMN IF NOT EXISTS attitude_comment TEXT NULL AFTER attitude_score,
    ADD COLUMN IF NOT EXISTS collaboration_score INT NULL AFTER attitude_comment,
    ADD COLUMN IF NOT EXISTS collaboration_comment TEXT NULL AFTER collaboration_score,
    ADD COLUMN IF NOT EXISTS creativity_score INT NULL AFTER collaboration_comment,
    ADD COLUMN IF NOT EXISTS creativity_comment TEXT NULL AFTER creativity_score,
    ADD COLUMN IF NOT EXISTS created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER creativity_comment,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME NULL AFTER created_at;

-- 결과 첨부 확인일 컬럼 추가
ALTER TABLE performance_attachment
    ADD COLUMN IF NOT EXISTS confirmed_at DATETIME NULL AFTER file_url;

-- 월간 점수 테이블 PK 컬럼명 보정이 필요한 경우 참고용
-- monthly_performance_id 컬럼이 없고 monthly_score_id만 있는 구버전 테이블이면 아래를 별도 검토 후 실행
-- ALTER TABLE monthly_performance CHANGE COLUMN monthly_score_id monthly_performance_id BIGINT NOT NULL AUTO_INCREMENT;

-- team_evaluation PK 컬럼이 없는 구버전 테이블이면 아래를 별도 검토 후 실행
-- ALTER TABLE team_evaluation
--     ADD COLUMN team_evaluation_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;
