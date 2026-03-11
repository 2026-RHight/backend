-- performance 모듈 스키마 보정용 수동 패치 SQL
-- 기존 DB에 schema.sql 변경 사항이 반영되지 않은 경우 실행한다.

-- 동료 평가 확장 컬럼 추가
ALTER TABLE peer_review
    ADD COLUMN IF NOT EXISTS culture_contribution INT NULL AFTER team_contribution;

-- 동료 평가 중복 방지
CREATE TABLE IF NOT EXISTS peer_review_duplicate_cleanup AS
SELECT *
FROM peer_review
WHERE 1 = 0;

INSERT INTO peer_review_duplicate_cleanup
SELECT pr.*
FROM peer_review pr
JOIN (
    SELECT eval_id, reviewer_id, MIN(peer_review_id) AS keep_peer_review_id
    FROM peer_review
    GROUP BY eval_id, reviewer_id
    HAVING COUNT(*) > 1
) dup
  ON dup.eval_id = pr.eval_id
 AND dup.reviewer_id = pr.reviewer_id
WHERE pr.peer_review_id <> dup.keep_peer_review_id;

DELETE pr
FROM peer_review pr
JOIN (
    SELECT eval_id, reviewer_id, MIN(peer_review_id) AS keep_peer_review_id
    FROM peer_review
    GROUP BY eval_id, reviewer_id
    HAVING COUNT(*) > 1
) dup
  ON dup.eval_id = pr.eval_id
 AND dup.reviewer_id = pr.reviewer_id
WHERE pr.peer_review_id <> dup.keep_peer_review_id;

SET @peer_review_constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'peer_review'
      AND constraint_name = 'ux_peer_review_eval_reviewer'
      AND constraint_type = 'UNIQUE'
);

SET @peer_review_constraint_sql = CASE
    WHEN @peer_review_constraint_exists = 0 THEN
        'ALTER TABLE peer_review ADD CONSTRAINT ux_peer_review_eval_reviewer UNIQUE (eval_id, reviewer_id)'
    ELSE
        'SELECT ''ux_peer_review_eval_reviewer already exists'' AS migration_status'
END;

PREPARE stmt_peer_review_constraint FROM @peer_review_constraint_sql;
EXECUTE stmt_peer_review_constraint;
DEALLOCATE PREPARE stmt_peer_review_constraint;

-- 팀 평가 확장 컬럼 추가
ALTER TABLE team_evaluation
    ADD COLUMN IF NOT EXISTS evaluation_year INT NULL AFTER appraisee_id,
    ADD COLUMN IF NOT EXISTS performance_score INT NULL AFTER evaluation_year,
    ADD COLUMN IF NOT EXISTS performance_comment TEXT NULL AFTER performance_score,
    ADD COLUMN IF NOT EXISTS attitude_score INT NULL AFTER performance_comment,
    ADD COLUMN IF NOT EXISTS attitude_comment TEXT NULL AFTER attitude_score,
    ADD COLUMN IF NOT EXISTS collaboration_score INT NULL AFTER attitude_comment,
    ADD COLUMN IF NOT EXISTS collaboration_comment TEXT NULL AFTER collaboration_score,
    ADD COLUMN IF NOT EXISTS creativity_score INT NULL AFTER collaboration_comment,
    ADD COLUMN IF NOT EXISTS creativity_comment TEXT NULL AFTER creativity_score,
    ADD COLUMN IF NOT EXISTS created_at DATETIME NULL AFTER creativity_comment,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME NULL AFTER created_at;

ALTER TABLE team_evaluation
    DROP COLUMN IF EXISTS performance_eval,
    DROP COLUMN IF EXISTS work_attitude_eval,
    DROP COLUMN IF EXISTS teamwork_eval,
    DROP COLUMN IF EXISTS solving_eval;

SET @team_evaluation_year_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'team_evaluation'
      AND index_name = 'idx_team_evaluation_year'
);

SET @team_evaluation_year_index_sql = CASE
    WHEN @team_evaluation_year_index_exists = 0 THEN
        'ALTER TABLE team_evaluation ADD INDEX idx_team_evaluation_year (evaluation_year)'
    ELSE
        'SELECT ''idx_team_evaluation_year already exists'' AS migration_status'
END;

PREPARE stmt_team_evaluation_year_index FROM @team_evaluation_year_index_sql;
EXECUTE stmt_team_evaluation_year_index;
DEALLOCATE PREPARE stmt_team_evaluation_year_index;

-- 결과 첨부 확인일 컬럼 추가
ALTER TABLE performance_attachment
    ADD COLUMN IF NOT EXISTS confirmed_at DATETIME NULL AFTER file_url;

-- evaluation.approval_id -> evaluation.evaluator_id 정리
SET @evaluation_has_approval_id = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'evaluation'
      AND column_name = 'approval_id'
);

SET @evaluation_has_evaluator_id = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'evaluation'
      AND column_name = 'evaluator_id'
);

SET @evaluation_rename_sql = CASE
    WHEN @evaluation_has_approval_id = 1 AND @evaluation_has_evaluator_id = 0 THEN
        'ALTER TABLE evaluation CHANGE COLUMN approval_id evaluator_id BIGINT NOT NULL'
    WHEN @evaluation_has_approval_id = 0 AND @evaluation_has_evaluator_id = 1 THEN
        'SELECT ''evaluation.evaluator_id is already aligned'' AS migration_status'
    ELSE
        'SIGNAL SQLSTATE ''45000'' SET MESSAGE_TEXT = ''evaluation key mismatch: expected exactly one of approval_id or evaluator_id before migration'''
END;

PREPARE stmt_evaluation_rename FROM @evaluation_rename_sql;
EXECUTE stmt_evaluation_rename;
DEALLOCATE PREPARE stmt_evaluation_rename;

-- monthly_performance / team_evaluation 키 불일치 자동 보정
-- monthly_performance.monthly_score_id -> monthly_performance.monthly_performance_id
SET @monthly_performance_has_new_id = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'monthly_performance'
      AND column_name = 'monthly_performance_id'
);

SET @monthly_performance_has_old_id = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'monthly_performance'
      AND column_name = 'monthly_score_id'
);

SET @monthly_performance_fix_sql = CASE
    WHEN @monthly_performance_has_new_id = 1 AND @monthly_performance_has_old_id = 0 THEN
        'SELECT ''monthly_performance_id is already aligned'' AS migration_status'
    WHEN @monthly_performance_has_new_id = 0 AND @monthly_performance_has_old_id = 1 THEN
        'ALTER TABLE monthly_performance CHANGE COLUMN monthly_score_id monthly_performance_id BIGINT NOT NULL AUTO_INCREMENT'
    ELSE
        'SIGNAL SQLSTATE ''45000'' SET MESSAGE_TEXT = ''monthly_performance key mismatch: expected exactly one of monthly_score_id or monthly_performance_id before migration'''
END;

PREPARE stmt_monthly_performance_fix FROM @monthly_performance_fix_sql;
EXECUTE stmt_monthly_performance_fix;
DEALLOCATE PREPARE stmt_monthly_performance_fix;

CREATE TABLE IF NOT EXISTS performance_metric_summary (
    performance_metric_summary_id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    metric_year INT NOT NULL,
    metric_month INT NOT NULL,
    personal_kpi_achievement_rate INT NOT NULL DEFAULT 0,
    team_kpi_achievement_rate INT NOT NULL DEFAULT 0,
    monthly_core_goal_progress_rate INT NOT NULL DEFAULT 0,
    score_change_rate DECIMAL(7,2) NOT NULL DEFAULT 0.00,
    composite_score INT NOT NULL DEFAULT 0,
    calculated_at DATETIME NOT NULL,
    PRIMARY KEY (performance_metric_summary_id),
    UNIQUE KEY uk_performance_metric_summary_employee_month (employee_id, metric_year, metric_month),
    KEY idx_performance_metric_summary_employee (employee_id),
    CONSTRAINT fk_performance_metric_summary_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

CREATE TABLE IF NOT EXISTS performance_weight (
    performance_weight_id BIGINT NOT NULL AUTO_INCREMENT,
    org_id BIGINT NOT NULL,
    personal_weight_rate INT NOT NULL DEFAULT 50,
    team_weight_rate INT NOT NULL DEFAULT 50,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    PRIMARY KEY (performance_weight_id),
    UNIQUE KEY uk_performance_weight_org (org_id),
    CONSTRAINT fk_performance_weight_org FOREIGN KEY (org_id) REFERENCES organization(org_id)
);

-- team_evaluation.team_evaluation_id PK 자동 보정
SET @team_evaluation_has_id_column = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'team_evaluation'
      AND column_name = 'team_evaluation_id'
);

SET @team_evaluation_pk_on_id = (
    SELECT COUNT(*)
    FROM information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu
      ON tc.constraint_schema = kcu.constraint_schema
     AND tc.table_name = kcu.table_name
     AND tc.constraint_name = kcu.constraint_name
    WHERE tc.constraint_schema = DATABASE()
      AND tc.table_name = 'team_evaluation'
      AND tc.constraint_type = 'PRIMARY KEY'
      AND kcu.column_name = 'team_evaluation_id'
);

SET @team_evaluation_has_any_pk = (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'team_evaluation'
      AND constraint_type = 'PRIMARY KEY'
);

SET @team_evaluation_fix_sql = CASE
    WHEN @team_evaluation_pk_on_id = 1 THEN
        'SELECT ''team_evaluation_id primary key is already aligned'' AS migration_status'
    WHEN @team_evaluation_has_id_column = 0 AND @team_evaluation_has_any_pk = 0 THEN
        'ALTER TABLE team_evaluation ADD COLUMN team_evaluation_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST'
    ELSE
        'SIGNAL SQLSTATE ''45000'' SET MESSAGE_TEXT = ''team_evaluation key mismatch: expected team_evaluation_id primary key or no primary key before migration'''
END;

PREPARE stmt_team_evaluation_fix FROM @team_evaluation_fix_sql;
EXECUTE stmt_team_evaluation_fix;
DEALLOCATE PREPARE stmt_team_evaluation_fix;

DROP TABLE IF EXISTS peer_review_duplicate_cleanup;
