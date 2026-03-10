-- payroll_ledger 테이블에 스냅샷 컬럼 추가 및 길이 조정 마이그레이션
-- 기존 DB에 컬럼이 없을 경우를 위해 ALTER TABLE 문 사용

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
