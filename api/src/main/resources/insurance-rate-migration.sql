-- insurance_rate 테이블의 long_term_care_rate 요율 체계 변경에 따른 마이그레이션
-- 기존: 소득 대비 요율 (약 0.00459) -> 변경: 건강보험료 대비 요율 (0.12950)

-- 1. 기존 테이블의 기본값 변경 (신규 행 삽입 시 적용)
ALTER TABLE insurance_rate MODIFY long_term_care_rate DECIMAL(7,5) NOT NULL DEFAULT 0.12950;

-- 2. 2024, 2025년도 기존 데이터 백필 (이미 데이터가 존재하는 경우)
UPDATE insurance_rate 
SET long_term_care_rate = 0.12950 
WHERE apply_year IN (2024, 2025);
