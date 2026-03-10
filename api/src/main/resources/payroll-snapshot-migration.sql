-- 0. year_month 컬럼을 target_month로 이름 변경 (기존 DB 호환)
-- MySQL 8.0+
SET @exist_year_month = (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'payroll_ledger' AND column_name = 'year_month' AND table_schema = DATABASE());
SET @rename_sql = IF(@exist_year_month > 0, 'ALTER TABLE payroll_ledger RENAME COLUMN year_month TO target_month', 'SELECT "target_month already exists"');
PREPARE stmt FROM @rename_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 인덱스 갱신
ALTER TABLE payroll_ledger DROP INDEX IF EXISTS idx_payroll_ledger_year_month;
ALTER TABLE payroll_ledger DROP INDEX IF EXISTS uk_payroll_ledger_employee_month; -- 이전 이름이 uk_payroll_ledger_employee_month일 수 있음

SET @exist_target_month_idx = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_name = 'payroll_ledger' AND index_name = 'idx_payroll_ledger_target_month' AND table_schema = DATABASE());
SET @idx_sql = IF(@exist_target_month_idx = 0, 'ALTER TABLE payroll_ledger ADD INDEX idx_payroll_ledger_target_month (target_month)', 'SELECT "index already exists"');
PREPARE stmt2 FROM @idx_sql;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

SET @exist_target_month_uk = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_name = 'payroll_ledger' AND index_name = 'uk_payroll_ledger_employee_month' AND table_schema = DATABASE());
SET @uk_sql = IF(@exist_target_month_uk = 0, 'ALTER TABLE payroll_ledger ADD UNIQUE KEY uk_payroll_ledger_employee_month (employee_id, target_month)', 'SELECT "unique key already exists"');
PREPARE stmt3 FROM @uk_sql;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

-- 1. 컬럼 추가 (존재하지 않을 경우 대비하여 개별 실행 권장하거나 프로시저 사용 가능하지만 보통 마이그레이션 툴에서 처리)
-- 여기서는 단순 ALTER TABLE 문으로 작성합니다.

ALTER TABLE payroll_ledger ADD COLUMN IF NOT EXISTS employee_name_snapshot VARCHAR(100);
ALTER TABLE payroll_ledger ADD COLUMN IF NOT EXISTS dept_name_snapshot VARCHAR(255);
ALTER TABLE payroll_ledger ADD COLUMN IF NOT EXISTS position_name_snapshot VARCHAR(255);
ALTER TABLE payroll_ledger ADD COLUMN IF NOT EXISTS bank_name_snapshot VARCHAR(50);
ALTER TABLE payroll_ledger ADD COLUMN IF NOT EXISTS account_number_snapshot_enc TEXT;
ALTER TABLE payroll_ledger ADD COLUMN IF NOT EXISTS account_holder_snapshot VARCHAR(100);

-- 2. 이미 컬럼이 존재할 경우 길이를 안전하게 확장 (Truncation 방지)
ALTER TABLE payroll_ledger MODIFY employee_name_snapshot VARCHAR(100);
ALTER TABLE payroll_ledger MODIFY dept_name_snapshot VARCHAR(255);
ALTER TABLE payroll_ledger MODIFY position_name_snapshot VARCHAR(255);
ALTER TABLE payroll_ledger MODIFY bank_name_snapshot VARCHAR(50);
ALTER TABLE payroll_ledger MODIFY account_number_snapshot_enc TEXT;
ALTER TABLE payroll_ledger MODIFY account_holder_snapshot VARCHAR(100);
