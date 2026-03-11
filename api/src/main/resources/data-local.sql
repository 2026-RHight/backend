-- Local development seed data
-- Safe to run multiple times (idempotent inserts).

-- ---------------------------------------------------------------------------
-- Roles
-- ---------------------------------------------------------------------------
INSERT INTO role (role_code, role_name, description)
SELECT 'EVALUATOR', '평가자', '평가자 권한'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM role WHERE role_code = 'EVALUATOR'
);

INSERT INTO role (role_code, role_name, description)
SELECT 'EVALUATEE', '피평가자', '피평가자 권한'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM role WHERE role_code = 'EVALUATEE'
);

INSERT INTO role (role_code, role_name, description)
SELECT 'HR_ADMIN_MASTER', '인사관리자-총괄', '인사 총괄 권한'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM role WHERE role_code = 'HR_ADMIN_MASTER'
);

INSERT INTO role (role_code, role_name, description)
SELECT 'HR_ADMIN_PAYROLL', '인사관리자-급여', '급여 관리 권한'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM role WHERE role_code = 'HR_ADMIN_PAYROLL'
);

INSERT INTO role (role_code, role_name, description)
SELECT 'HR_ADMIN_BASIC', '인사관리자-기본', '기본 인사 관리 권한'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM role WHERE role_code = 'HR_ADMIN_BASIC'
);

-- ---------------------------------------------------------------------------
-- HR files (profile)
-- ---------------------------------------------------------------------------
INSERT INTO hr_file (hr_file_id, file_url, file_title)
SELECT 1, 'https://cdn.rhight.local/profile/kimgwanri.png', '김관리 프로필'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM hr_file WHERE hr_file_id = 1
);

INSERT INTO hr_file (hr_file_id, file_url, file_title)
SELECT 2, 'https://cdn.rhight.local/profile/parkilban.png', '박일반 프로필'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM hr_file WHERE hr_file_id = 2
);

-- ---------------------------------------------------------------------------
-- Organization tree (회사 > 본부 > 센터 > 부 > 팀)
-- ---------------------------------------------------------------------------
INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 1, 'RHight Inc.', 'COMPANY', NULL, 1, 1, TRUE
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM organization WHERE org_id = 1
);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 2, '경영지원본부', 'HEADQUARTER', 1, 2, 1, TRUE
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM organization WHERE org_id = 2
);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 3, '인사팀', 'TEAM', 2, 3, 1, TRUE
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM organization WHERE org_id = 3
);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 4, '총무팀', 'TEAM', 2, 3, 2, TRUE
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM organization WHERE org_id = 4
);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 5, '마케팅팀', 'TEAM', 2, 3, 3, TRUE
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM organization WHERE org_id = 5
);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 6, '기술연구소', 'HEADQUARTER', 1, 2, 2, TRUE
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM organization WHERE org_id = 6
);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 7, 'SW개발센터', 'CENTER', 6, 3, 1, TRUE
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM organization WHERE org_id = 7
);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 8, '개발부', 'DEPARTMENT', 7, 4, 1, TRUE
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM organization WHERE org_id = 8
);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9, '개발1팀', 'TEAM', 8, 5, 1, TRUE
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM organization WHERE org_id = 9
);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 10, '개발2팀', 'TEAM', 8, 5, 2, TRUE
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM organization WHERE org_id = 10
);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 11, '모바일1팀', 'TEAM', 8, 5, 3, TRUE
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM organization WHERE org_id = 11
);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 12, 'QA팀', 'TEAM', 8, 5, 4, TRUE
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM organization WHERE org_id = 12
);

-- ---------------------------------------------------------------------------
-- Organization closure (ancestor-descendant)
-- ---------------------------------------------------------------------------
/*TODO : 클로이 (org_closure 배포 반영)

- 현재 org_closure는 로컬 시드(data-local.sql)에서만 백필됨.
- 운영/개발 DB에는 org_closure가 비어 있을 수 있으므로, 배포 확정 시 아래 작업을 반드시 추가한다.

1) 배포 마이그레이션에 org_closure 1회 백필 SQL 추가
- organization 기준으로 closure 재생성
- 권장 순서:
  - DELETE FROM org_closure;
  - WITH RECURSIVE ... INSERT INTO org_closure ...

2) 배포 파이프라인에 마이그레이션 실행 단계 추가
- Flyway/Liquibase 도입 또는 배포 스크립트에서 SQL 1회 실행

3) 추후 조직 변경 기능(생성/이동/비활성화) 개발 시
- 트랜잭션 내에서 org_closure 갱신 로직 함께 구현
- 또는 DB 트리거/프로시저로 정합성 보장

검증 쿼리
- SELECT COUNT(*) FROM org_closure;
- 0 건이면 백필 누락으로 판단
*/

DELETE FROM org_closure;

INSERT INTO org_closure (ancestor_org_id, descendant_org_id, depth, sort_path)
WITH RECURSIVE tree AS (
    SELECT o.org_id,
           o.parent_org_id,
           CAST(
               CONCAT(
                       LPAD(COALESCE(o.sort_order, 0), 4, '0'),
                       '-',
                       LPAD(o.org_id, 10, '0')
               ) AS CHAR(2000)
           ) AS sort_path
    FROM organization o
    WHERE o.parent_org_id IS NULL
      AND o.is_active = TRUE

    UNION ALL

    SELECT c.org_id,
           c.parent_org_id,
           CAST(
               CONCAT(
                       p.sort_path,
                       '/',
                       LPAD(COALESCE(c.sort_order, 0), 4, '0'),
                       '-',
                       LPAD(c.org_id, 10, '0')
               ) AS CHAR(2000)
           ) AS sort_path
    FROM organization c
    JOIN tree p ON c.parent_org_id = p.org_id
    WHERE c.is_active = TRUE
),
closure AS (
    SELECT t.org_id AS ancestor_org_id,
           t.org_id AS descendant_org_id,
           0 AS depth
    FROM tree t

    UNION ALL

    SELECT c.ancestor_org_id,
           child.org_id AS descendant_org_id,
           c.depth + 1 AS depth
    FROM closure c
    JOIN organization child ON child.parent_org_id = c.descendant_org_id
    WHERE child.is_active = TRUE
)
SELECT c.ancestor_org_id,
       c.descendant_org_id,
       c.depth,
       t.sort_path
FROM closure c
JOIN tree t ON t.org_id = c.descendant_org_id;

-- ---------------------------------------------------------------------------
-- HR master data
-- ---------------------------------------------------------------------------
INSERT INTO hr_position (position_name)
SELECT '팀장'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM hr_position WHERE position_name = '팀장'
);

INSERT INTO hr_position (position_name)
SELECT '팀원'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM hr_position WHERE position_name = '팀원'
);

INSERT INTO hr_rank (rank_name, rank_no)
SELECT '과장', 4
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM hr_rank WHERE rank_name = '과장'
);

INSERT INTO hr_rank (rank_name, rank_no)
SELECT '대리', 3
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM hr_rank WHERE rank_name = '대리'
);

INSERT INTO job (job_name)
SELECT '백엔드 개발자'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM job WHERE job_name = '백엔드 개발자'
);

INSERT INTO working_area (area_name)
SELECT '서울 강남'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM working_area WHERE area_name = '서울 강남'
);

-- ---------------------------------------------------------------------------
-- Employees
-- ---------------------------------------------------------------------------
INSERT INTO employee (
    employee_num,
    employee_name,
    employee_password,
    phone,
    ext,
    email,
    address,
    birth_date,
    bank_name,
    account_number_enc,
    account_number_hash,
    resident_number_enc,
    resident_number_hash,
    initial_state,
    employ_state,
    hire_date,
    profile_id
)
SELECT
    '2402040001',
    '김관리',
    '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01000000001',
    '1001',
    'admin1@example.test',
    '서울시 테스트구 테스트로 1',
    '1990-01-01',
    '국민은행',
    'QCxP2/svQ4ulFUned7Cd1WduR35l42J2ghA7SS8J+e80Ty5Bc11q6A==',
    '181d7c608da830efe255c510be09d2096a5599e7540e310adde70ab50f361ff4',
    'pASZ4pw5u3YEpZzlHmuTjqjwtKJYAInm8FBXZULqbzN/U5DV6dd6rcg=',
    'd251350014ec875cc4a093809abb639e05c97334ebf284222b0c82cfc6971df3',
    false,
    'WORK',
    '2024-02-04',
    1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM employee WHERE employee_num = '2402040001'
);

INSERT INTO employee (
    employee_num,
    employee_name,
    employee_password,
    phone,
    ext,
    email,
    address,
    birth_date,
    bank_name,
    account_number_enc,
    account_number_hash,
    resident_number_enc,
    resident_number_hash,
    initial_state,
    employ_state,
    hire_date,
    profile_id
)
SELECT
    '2402040002',
    '박일반',
    '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01000000002',
    '1002',
    'user2@example.test',
    '서울시 테스트구 테스트로 2',
    '1992-01-01',
    '국민은행',
    'QZCbN7Ym2lcf431cuPeowELldIaGTLfh48V8eEAuCF9YVuysk88ijQ==',
    'd5b3d6656064e2eb9c49b1ffc6c60fc031cda8e8c87ea290ec2478591654f223',
    'g4HK9BtfQBLVwnBjOQnC9e1rmRxgUaclDJCJzjc9vBwKVsG0O388JB0=',
    '0cba2dac201b09cb86d5c957907426681a45300220ab5240a22e84d248004978',
    false,
    'WORK',
    '2025-01-01',
    2
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM employee WHERE employee_num = '2402040002'
);

-- ---------------------------------------------------------------------------
-- Employee HR info (current assignment)
-- ---------------------------------------------------------------------------
INSERT INTO employee_hr_info (
    employee_id,
    org_id,
    effective_from,
    position_id,
    rank_id,
    job_id,
    employ_type,
    recruit_type,
    area_id
)
SELECT
    e.employee_id,
    o.org_id,
    DATE '2024-02-01',
    p.position_id,
    r.rank_id,
    j.job_id,
    'REGULAR',
    'EXPERIENCED',
    a.area_id
FROM employee e
JOIN organization o ON o.org_name = '인사팀'
JOIN hr_position p ON p.position_name = '팀장'
JOIN hr_rank r ON r.rank_name = '과장'
JOIN job j ON j.job_name = '백엔드 개발자'
JOIN working_area a ON a.area_name = '서울 강남'
WHERE e.employee_num = '2402040001'
  AND NOT EXISTS (
      SELECT 1
      FROM employee_hr_info h
      WHERE h.employee_id = e.employee_id
  );

INSERT INTO employee_hr_info (
    employee_id,
    org_id,
    effective_from,
    position_id,
    rank_id,
    job_id,
    employ_type,
    recruit_type,
    area_id
)
SELECT
    e.employee_id,
    o.org_id,
    DATE '2025-01-01',
    p.position_id,
    r.rank_id,
    j.job_id,
    'REGULAR',
    'NEW',
    a.area_id
FROM employee e
JOIN organization o ON o.org_name = '개발1팀'
JOIN hr_position p ON p.position_name = '팀원'
JOIN hr_rank r ON r.rank_name = '대리'
JOIN job j ON j.job_name = '백엔드 개발자'
JOIN working_area a ON a.area_name = '서울 강남'
WHERE e.employee_num = '2402040002'
  AND NOT EXISTS (
      SELECT 1
      FROM employee_hr_info h
      WHERE h.employee_id = e.employee_id
  );

-- ---------------------------------------------------------------------------
-- Employee role mapping
-- ---------------------------------------------------------------------------
INSERT INTO employee_role (employee_id, role_id)
SELECT e.employee_id, r.role_id
FROM employee e
JOIN role r ON r.role_code = 'HR_ADMIN_BASIC'
WHERE e.employee_num = '2402040001'
  AND NOT EXISTS (
      SELECT 1
      FROM employee_role er
      WHERE er.employee_id = e.employee_id
        AND er.role_id = r.role_id
  );

INSERT INTO employee_role (employee_id, role_id)
SELECT e.employee_id, r.role_id
FROM employee e
JOIN role r ON r.role_code = 'EVALUATEE'
WHERE e.employee_num = '2402040002'
  AND NOT EXISTS (
      SELECT 1
      FROM employee_role er
      WHERE er.employee_id = e.employee_id
        AND er.role_id = r.role_id
  );

-- ---------------------------------------------------------------------------
-- Password history
-- ---------------------------------------------------------------------------
INSERT INTO password_history (employee_id, password_hash, change_at)
SELECT e.employee_id,
       '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
       NOW()
FROM employee e
WHERE e.employee_num = '2402040001'
  AND NOT EXISTS (
      SELECT 1
      FROM password_history ph
      WHERE ph.employee_id = e.employee_id
  );

INSERT INTO password_history (employee_id, password_hash, change_at)
SELECT e.employee_id,
       '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
       NOW()
FROM employee e
WHERE e.employee_num = '2402040002'
  AND NOT EXISTS (
      SELECT 1
      FROM password_history ph
      WHERE ph.employee_id = e.employee_id
  );


-- ==========================================
-- 1. [필수 기본 데이터] 프로필 파일 및 사원 세팅
-- ==========================================

-- 프로필 이미지
INSERT INTO hr_file (file_url, file_title)
VALUES ('http://dummy.com/profile1.jpg', '기본 프로필 이미지');

-- 1번 사원: 김개발 (일반 팀원)
INSERT INTO employee (
    employee_num, employee_name, employee_password, phone, ext, email, address,
    birth_date, bank_name, account_number_enc, account_number_hash,
    resident_number_enc, resident_number_hash, initial_state, employ_state, profile_id
) VALUES (
             'EMP2026001', '김개발', 'hashed_pwd_123', '010-1234-5678', '1234', 'dev@reverse.com', '서울시 강남구 테헤란로',
             '1995-05-05', '국민은행', 'enc_acc_1', 'hash_acc_1',
             'enc_res_1', 'hash_res_1', true, 'WORK', 1
         );

-- 2번 사원: 이팀장 (관리자 결재 테스트용)
INSERT INTO employee (
    employee_num, employee_name, employee_password, phone, ext, email, address,
    birth_date, bank_name, account_number_enc, account_number_hash,
    resident_number_enc, resident_number_hash, initial_state, employ_state, profile_id
) VALUES (
             'EMP2026002', '이팀장', 'hashed_pwd_456', '010-9876-5432', '5678', 'leader@reverse.com', '서울시 서초구 강남대로',
             '1985-10-10', '신한은행', 'enc_acc_2', 'hash_acc_2',
             'enc_res_2', 'hash_res_2', true, 'WORK', 1
         );


-- ==========================================
-- 2. [근태 도메인] 테스트 데이터
-- ==========================================

-- 연차 부여
INSERT INTO leave_balance (employee_id, base_year, total_annual_leave, used_annual_leave)
SELECT e.employee_id, 2026, 15.0, 0.0
FROM employee e
WHERE e.employee_num = 'EMP2026001'
  AND NOT EXISTS (
      SELECT 1
      FROM leave_balance lb
      WHERE lb.employee_id = e.employee_id
        AND lb.base_year = 2026
  );

INSERT INTO leave_balance (employee_id, base_year, total_annual_leave, used_annual_leave)
SELECT e.employee_id, 2026, 20.0, 0.0
FROM employee e
WHERE e.employee_num = 'EMP2026002'
  AND NOT EXISTS (
      SELECT 1
      FROM leave_balance lb
      WHERE lb.employee_id = e.employee_id
        AND lb.base_year = 2026
  );

-- 출퇴근 기록
INSERT INTO attendance_record (
    employee_id, work_date, check_in_time, check_out_time, status, tardy_reason, modify_reason,
    overtime_hours, night_work_hours, holiday_work_hours, is_unpaid_leave, is_closed
)
SELECT e.employee_id, '2026-03-04', '08:50:00', '18:05:00', 'NORMAL', NULL, NULL, 0.0, 0.0, 0.0, FALSE, FALSE
FROM employee e
WHERE e.employee_num = 'EMP2026001'
  AND NOT EXISTS (
      SELECT 1
      FROM attendance_record ar
      WHERE ar.employee_id = e.employee_id
        AND ar.work_date = '2026-03-04'
  );

INSERT INTO attendance_record (
    employee_id, work_date, check_in_time, check_out_time, status, tardy_reason, modify_reason,
    overtime_hours, night_work_hours, holiday_work_hours, is_unpaid_leave, is_closed
)
SELECT e.employee_id, '2026-03-04', '09:15:00', NULL, 'TARDY', '지하철 연착', NULL, 0.0, 0.0, 0.0, FALSE, FALSE
FROM employee e
WHERE e.employee_num = 'EMP2026002'
  AND NOT EXISTS (
      SELECT 1
      FROM attendance_record ar
      WHERE ar.employee_id = e.employee_id
        AND ar.work_date = '2026-03-04'
  );


-- ==========================================
-- 3. [휴가/출장/연장근무] 결재 테스트 데이터
-- ==========================================

-- 휴가 신청
INSERT INTO leave_request (employee_id, start_date, end_date, leave_type, leave_status, used_days, reason, reject_reason)
VALUES (1, '2026-03-10', '2026-03-10', 'ANNUAL', 'APPROVED', 1.0, '개인 사정', NULL);

INSERT INTO leave_request (employee_id, start_date, end_date, leave_type, leave_status, used_days, reason, reject_reason)
VALUES (1, '2026-03-15', '2026-03-15', 'HALF_PM', 'PENDING', 0.5, '병원 진료', NULL);

-- 외근 신청
INSERT INTO business_trip_request (employee_id, trip_type, destination, start_datetime, end_datetime, reason, approval_status, reject_reason)
VALUES (1, 'OUTSIDE_WORK', '고객사(A사) 미팅', '2026-03-05 14:00:00', '2026-03-05 18:00:00', '프로젝트 킥오프 미팅', 'APPROVED', NULL);

-- 연장근무 신청
INSERT INTO overtime_request (employee_id, work_date, start_time, end_time, reason, approval_status, reject_reason)
VALUES (1, '2026-03-04', '2026-03-04 18:00:00', '2026-03-04 20:00:00', '긴급 서버 버그 수정', 'PENDING', NULL);
-- ---------------------------------------------------------------------------
-- HR files (skill/career attachments)
-- ---------------------------------------------------------------------------
INSERT INTO hr_file (hr_file_id, file_url, file_title)
SELECT 101, 'https://cdn.rhight.local/hr/skill/certificate-001.pdf', '자격증 증빙 1'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM hr_file WHERE hr_file_id = 101
);

INSERT INTO hr_file (hr_file_id, file_url, file_title)
SELECT 102, 'https://cdn.rhight.local/hr/skill/language-001.pdf', '어학 증빙 1'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM hr_file WHERE hr_file_id = 102
);

INSERT INTO hr_file (hr_file_id, file_url, file_title)
SELECT 103, 'https://cdn.rhight.local/hr/skill/license-001.pdf', '면허 증빙 1'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM hr_file WHERE hr_file_id = 103
);

INSERT INTO hr_file (hr_file_id, file_url, file_title)
SELECT 201, 'https://cdn.rhight.local/hr/career/career-001.pdf', '경력 증빙 1'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM hr_file WHERE hr_file_id = 201
);

INSERT INTO hr_file (hr_file_id, file_url, file_title)
SELECT 202, 'https://cdn.rhight.local/hr/career/career-002.pdf', '경력 증빙 2'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM hr_file WHERE hr_file_id = 202
);

INSERT INTO hr_file (hr_file_id, file_url, file_title)
SELECT 203, 'https://cdn.rhight.local/hr/career/career-003.pdf', '경력 증빙 3'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM hr_file WHERE hr_file_id = 203
);

-- ---------------------------------------------------------------------------
-- Skills (for MyPage initial rendering)
-- ---------------------------------------------------------------------------
INSERT INTO employee_skill_credential (
    employee_id,
    category,
    skill_name,
    acquisition_date,
    license_number,
    hr_file_id
)
SELECT
    e.employee_id,
    'CERTIFICATE',
    '정보처리기사',
    DATE '2023-06-01',
    'CERT-2023-0001',
    101
FROM employee e
WHERE e.employee_num = '2402040001'
  AND NOT EXISTS (
      SELECT 1
      FROM employee_skill_credential s
      WHERE s.employee_id = e.employee_id
        AND s.skill_name = '정보처리기사'
  );

INSERT INTO employee_skill_credential (
    employee_id,
    category,
    skill_name,
    acquisition_date,
    license_number,
    hr_file_id
)
SELECT
    e.employee_id,
    'LANGUAGE',
    'TOEIC 920',
    DATE '2024-01-01',
    NULL,
    102
FROM employee e
WHERE e.employee_num = '2402040001'
  AND NOT EXISTS (
      SELECT 1
      FROM employee_skill_credential s
      WHERE s.employee_id = e.employee_id
        AND s.skill_name = 'TOEIC 920'
  );

INSERT INTO employee_skill_credential (
    employee_id,
    category,
    skill_name,
    acquisition_date,
    license_number,
    hr_file_id
)
SELECT
    e.employee_id,
    'CERTIFICATE',
    'SQLD',
    DATE '2023-09-01',
    'SQLD-2023-0007',
    103
FROM employee e
WHERE e.employee_num = '2402040001'
  AND NOT EXISTS (
      SELECT 1
      FROM employee_skill_credential s
      WHERE s.employee_id = e.employee_id
        AND s.skill_name = 'SQLD'
  );

-- ---------------------------------------------------------------------------
-- Career details (for MyPage initial rendering)
-- ---------------------------------------------------------------------------
INSERT INTO employee_career_details (
    employee_id,
    company_name,
    org_name,
    start_date,
    end_date,
    hr_file_id
)
SELECT
    e.employee_id,
    'RHight',
    '백엔드 개발자 · 모바일팀',
    DATE '2024-02-01',
    NULL,
    201
FROM employee e
WHERE e.employee_num = '2402040001'
  AND NOT EXISTS (
      SELECT 1
      FROM employee_career_details c
      WHERE c.employee_id = e.employee_id
        AND c.company_name = 'RHight'
        AND c.start_date = DATE '2024-02-01'
  );

INSERT INTO employee_career_details (
    employee_id,
    company_name,
    org_name,
    start_date,
    end_date,
    hr_file_id
)
SELECT
    e.employee_id,
    'Example Platform',
    '서버 개발자 · 플랫폼개발팀',
    DATE '2021-03-01',
    DATE '2024-01-31',
    202
FROM employee e
WHERE e.employee_num = '2402040001'
  AND NOT EXISTS (
      SELECT 1
      FROM employee_career_details c
      WHERE c.employee_id = e.employee_id
        AND c.company_name = 'Example Platform'
        AND c.start_date = DATE '2021-03-01'
  );

INSERT INTO employee_career_details (
    employee_id,
    company_name,
    org_name,
    start_date,
    end_date,
    hr_file_id
)
SELECT
    e.employee_id,
    'Example Service',
    '백엔드 개발자 · 서비스개발팀',
    DATE '2019-07-01',
    DATE '2021-02-28',
    203
FROM employee e
WHERE e.employee_num = '2402040001'
  AND NOT EXISTS (
      SELECT 1
      FROM employee_career_details c
      WHERE c.employee_id = e.employee_id
        AND c.company_name = 'Example Service'
        AND c.start_date = DATE '2019-07-01'
  );

-- ---------------------------------------------------------------------------
-- HR events (for MyPage HR history tab)
-- ---------------------------------------------------------------------------



-- ---------------------------------------------------------------------------
-- Organization chart demo seeds (tree + members)
-- ---------------------------------------------------------------------------
INSERT INTO hr_position (position_name)
SELECT '본부장'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM hr_position WHERE position_name = '본부장'
);

INSERT INTO hr_rank (rank_name, rank_no)
SELECT '주임', 2
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM hr_rank WHERE rank_name = '주임'
);

INSERT INTO hr_rank (rank_name, rank_no)
SELECT '사원', 1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM hr_rank WHERE rank_name = '사원'
);

INSERT INTO job (job_name)
SELECT '프론트엔드 개발자'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM job WHERE job_name = '프론트엔드 개발자'
);

INSERT INTO job (job_name)
SELECT 'QA 엔지니어'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM job WHERE job_name = 'QA 엔지니어'
);

INSERT INTO job (job_name)
SELECT '인사 평가'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM job WHERE job_name = '인사 평가'
);

INSERT INTO job (job_name)
SELECT '인사 기획'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM job WHERE job_name = '인사 기획'
);

INSERT INTO job (job_name)
SELECT '인사 운영'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM job WHERE job_name = '인사 운영'
);

INSERT INTO job (job_name)
SELECT '채용 운영'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM job WHERE job_name = '채용 운영'
);

INSERT INTO employee (
    employee_num, employee_name, employee_password, phone, ext, email, address, birth_date,
    bank_name, account_number_enc, account_number_hash, resident_number_enc, resident_number_hash,
    initial_state, employ_state, hire_date, profile_id
)
SELECT
    '2402040003', '박민지', '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01000000003', '1003', 'minji.park@example.test', '서울시 테스트구 테스트로 3', '1995-03-28',
    '국민은행', 'QCxP2/svQ4ulFUned7Cd1WduR35l42J2ghA7SS8J+e80Ty5Bc11q6A==',
    '181d7c608da830efe255c510be09d2096a5599e7540e310adde70ab50f361ff4',
    'pASZ4pw5u3YEpZzlHmuTjqjwtKJYAInm8FBXZULqbzN/U5DV6dd6rcg=',
    'd251350014ec875cc4a093809abb639e05c97334ebf284222b0c82cfc6971df3',
    false, 'WORK', '2024-03-01', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE employee_num = '2402040003');

INSERT INTO employee (
    employee_num, employee_name, employee_password, phone, ext, email, address, birth_date,
    bank_name, account_number_enc, account_number_hash, resident_number_enc, resident_number_hash,
    initial_state, employ_state, hire_date, profile_id
)
SELECT
    '2402040004', '이준호', '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01000000004', '1004', 'junho.lee@example.test', '서울시 테스트구 테스트로 4', '1994-01-10',
    '국민은행', 'QCxP2/svQ4ulFUned7Cd1WduR35l42J2ghA7SS8J+e80Ty5Bc11q6A==',
    '181d7c608da830efe255c510be09d2096a5599e7540e310adde70ab50f361ff4',
    'pASZ4pw5u3YEpZzlHmuTjqjwtKJYAInm8FBXZULqbzN/U5DV6dd6rcg=',
    'd251350014ec875cc4a093809abb639e05c97334ebf284222b0c82cfc6971df3',
    false, 'WORK', '2024-04-01', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE employee_num = '2402040004');

INSERT INTO employee (
    employee_num, employee_name, employee_password, phone, ext, email, address, birth_date,
    bank_name, account_number_enc, account_number_hash, resident_number_enc, resident_number_hash,
    initial_state, employ_state, hire_date, profile_id
)
SELECT
    '2402040005', '최수빈', '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01000000005', '1005', 'subin.choi@example.test', '서울시 테스트구 테스트로 5', '1998-07-21',
    '국민은행', 'QCxP2/svQ4ulFUned7Cd1WduR35l42J2ghA7SS8J+e80Ty5Bc11q6A==',
    '181d7c608da830efe255c510be09d2096a5599e7540e310adde70ab50f361ff4',
    'pASZ4pw5u3YEpZzlHmuTjqjwtKJYAInm8FBXZULqbzN/U5DV6dd6rcg=',
    'd251350014ec875cc4a093809abb639e05c97334ebf284222b0c82cfc6971df3',
    false, 'WORK', '2024-06-01', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE employee_num = '2402040005');

INSERT INTO employee (
    employee_num, employee_name, employee_password, phone, ext, email, address, birth_date,
    bank_name, account_number_enc, account_number_hash, resident_number_enc, resident_number_hash,
    initial_state, employ_state, hire_date, profile_id
)
SELECT
    '2402040006', '정수진', '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01000000006', '1006', 'sujin.jung@example.test', '서울시 테스트구 테스트로 6', '1991-05-11',
    '국민은행', 'QCxP2/svQ4ulFUned7Cd1WduR35l42J2ghA7SS8J+e80Ty5Bc11q6A==',
    '181d7c608da830efe255c510be09d2096a5599e7540e310adde70ab50f361ff4',
    'pASZ4pw5u3YEpZzlHmuTjqjwtKJYAInm8FBXZULqbzN/U5DV6dd6rcg=',
    'd251350014ec875cc4a093809abb639e05c97334ebf284222b0c82cfc6971df3',
    false, 'WORK', '2023-01-01', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE employee_num = '2402040006');

INSERT INTO employee (
    employee_num, employee_name, employee_password, phone, ext, email, address, birth_date,
    bank_name, account_number_enc, account_number_hash, resident_number_enc, resident_number_hash,
    initial_state, employ_state, hire_date, profile_id
)
SELECT
    '2402040007', '최유진', '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01000000007', '1007', 'yujin.choi@example.test', '서울시 테스트구 테스트로 7', '1989-11-02',
    '국민은행', 'QCxP2/svQ4ulFUned7Cd1WduR35l42J2ghA7SS8J+e80Ty5Bc11q6A==',
    '181d7c608da830efe255c510be09d2096a5599e7540e310adde70ab50f361ff4',
    'pASZ4pw5u3YEpZzlHmuTjqjwtKJYAInm8FBXZULqbzN/U5DV6dd6rcg=',
    'd251350014ec875cc4a093809abb639e05c97334ebf284222b0c82cfc6971df3',
    false, 'WORK', '2022-09-01', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE employee_num = '2402040007');

INSERT INTO employee (
    employee_num, employee_name, employee_password, phone, ext, email, address, birth_date,
    bank_name, account_number_enc, account_number_hash, resident_number_enc, resident_number_hash,
    initial_state, employ_state, hire_date, profile_id
)
SELECT
    '2402040008', '김철수', '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01000000008', '1008', 'chulsoo.kim@example.test', '서울시 테스트구 테스트로 8', '1993-08-14',
    '국민은행', 'QCxP2/svQ4ulFUned7Cd1WduR35l42J2ghA7SS8J+e80Ty5Bc11q6A==',
    '181d7c608da830efe255c510be09d2096a5599e7540e310adde70ab50f361ff4',
    'pASZ4pw5u3YEpZzlHmuTjqjwtKJYAInm8FBXZULqbzN/U5DV6dd6rcg=',
    'd251350014ec875cc4a093809abb639e05c97334ebf284222b0c82cfc6971df3',
    false, 'WORK', '2023-03-01', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE employee_num = '2402040008');

INSERT INTO employee (
    employee_num, employee_name, employee_password, phone, ext, email, address, birth_date,
    bank_name, account_number_enc, account_number_hash, resident_number_enc, resident_number_hash,
    initial_state, employ_state, hire_date, profile_id
)
SELECT
    '2402040009', '홍길동', '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01000000009', '1009', 'gildong.hong@example.test', '서울시 테스트구 테스트로 9', '1997-02-25',
    '국민은행', 'QCxP2/svQ4ulFUned7Cd1WduR35l42J2ghA7SS8J+e80Ty5Bc11q6A==',
    '181d7c608da830efe255c510be09d2096a5599e7540e310adde70ab50f361ff4',
    'pASZ4pw5u3YEpZzlHmuTjqjwtKJYAInm8FBXZULqbzN/U5DV6dd6rcg=',
    'd251350014ec875cc4a093809abb639e05c97334ebf284222b0c82cfc6971df3',
    false, 'WORK', '2024-07-01', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE employee_num = '2402040009');

INSERT INTO employee (
    employee_num, employee_name, employee_password, phone, ext, email, address, birth_date,
    bank_name, account_number_enc, account_number_hash, resident_number_enc, resident_number_hash,
    initial_state, employ_state, hire_date, profile_id
)
SELECT
    '2402040010', '장원영', '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01000000010', '1010', 'wonyoung.jang@example.test', '서울시 테스트구 테스트로 10', '1999-12-30',
    '국민은행', 'QCxP2/svQ4ulFUned7Cd1WduR35l42J2ghA7SS8J+e80Ty5Bc11q6A==',
    '181d7c608da830efe255c510be09d2096a5599e7540e310adde70ab50f361ff4',
    'pASZ4pw5u3YEpZzlHmuTjqjwtKJYAInm8FBXZULqbzN/U5DV6dd6rcg=',
    'd251350014ec875cc4a093809abb639e05c97334ebf284222b0c82cfc6971df3',
    false, 'WORK', '2024-09-01', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE employee_num = '2402040010');

INSERT INTO employee (
    employee_num, employee_name, employee_password, phone, ext, email, address, birth_date,
    bank_name, account_number_enc, account_number_hash, resident_number_enc, resident_number_hash,
    initial_state, employ_state, hire_date, profile_id
)
SELECT
    '2402040011', '송혜교', '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01000000011', '1011', 'hyegyo.song@example.test', '서울시 테스트구 테스트로 11', '1987-01-05',
    '국민은행', 'QCxP2/svQ4ulFUned7Cd1WduR35l42J2ghA7SS8J+e80Ty5Bc11q6A==',
    '181d7c608da830efe255c510be09d2096a5599e7540e310adde70ab50f361ff4',
    'pASZ4pw5u3YEpZzlHmuTjqjwtKJYAInm8FBXZULqbzN/U5DV6dd6rcg=',
    'd251350014ec875cc4a093809abb639e05c97334ebf284222b0c82cfc6971df3',
    false, 'WORK', '2020-03-01', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE employee_num = '2402040011');

INSERT INTO employee (
    employee_num, employee_name, employee_password, phone, ext, email, address, birth_date,
    bank_name, account_number_enc, account_number_hash, resident_number_enc, resident_number_hash,
    initial_state, employ_state, hire_date, profile_id
)
SELECT
    '2402040012', '강과장', '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01000000012', '1012', 'gwajang.kang@example.test', '서울시 테스트구 테스트로 12', '1990-04-18',
    '국민은행', 'QCxP2/svQ4ulFUned7Cd1WduR35l42J2ghA7SS8J+e80Ty5Bc11q6A==',
    '181d7c608da830efe255c510be09d2096a5599e7540e310adde70ab50f361ff4',
    'pASZ4pw5u3YEpZzlHmuTjqjwtKJYAInm8FBXZULqbzN/U5DV6dd6rcg=',
    'd251350014ec875cc4a093809abb639e05c97334ebf284222b0c82cfc6971df3',
    false, 'WORK', '2023-05-01', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE employee_num = '2402040012');

INSERT INTO employee (
    employee_num, employee_name, employee_password, phone, ext, email, address, birth_date,
    bank_name, account_number_enc, account_number_hash, resident_number_enc, resident_number_hash,
    initial_state, employ_state, hire_date, profile_id
)
SELECT
    '2402040013', '남대리', '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01000000013', '1013', 'daeri.nam@example.test', '서울시 테스트구 테스트로 13', '1992-06-11',
    '국민은행', 'QCxP2/svQ4ulFUned7Cd1WduR35l42J2ghA7SS8J+e80Ty5Bc11q6A==',
    '181d7c608da830efe255c510be09d2096a5599e7540e310adde70ab50f361ff4',
    'pASZ4pw5u3YEpZzlHmuTjqjwtKJYAInm8FBXZULqbzN/U5DV6dd6rcg=',
    'd251350014ec875cc4a093809abb639e05c97334ebf284222b0c82cfc6971df3',
    false, 'WORK', '2023-08-01', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE employee_num = '2402040013');

INSERT INTO employee (
    employee_num, employee_name, employee_password, phone, ext, email, address, birth_date,
    bank_name, account_number_enc, account_number_hash, resident_number_enc, resident_number_hash,
    initial_state, employ_state, hire_date, profile_id
)
SELECT
    '2402040014', '오주임', '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01000000014', '1014', 'juim.oh@example.test', '서울시 테스트구 테스트로 14', '1996-10-03',
    '국민은행', 'QCxP2/svQ4ulFUned7Cd1WduR35l42J2ghA7SS8J+e80Ty5Bc11q6A==',
    '181d7c608da830efe255c510be09d2096a5599e7540e310adde70ab50f361ff4',
    'pASZ4pw5u3YEpZzlHmuTjqjwtKJYAInm8FBXZULqbzN/U5DV6dd6rcg=',
    'd251350014ec875cc4a093809abb639e05c97334ebf284222b0c82cfc6971df3',
    false, 'WORK', '2024-01-01', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE employee_num = '2402040014');

INSERT INTO employee (
    employee_num, employee_name, employee_password, phone, ext, email, address, birth_date,
    bank_name, account_number_enc, account_number_hash, resident_number_enc, resident_number_hash,
    initial_state, employ_state, hire_date, profile_id
)
SELECT
    '2402040015', '한사원', '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01000000015', '1015', 'sawon.han@example.test', '서울시 테스트구 테스트로 15', '1999-09-09',
    '국민은행', 'QCxP2/svQ4ulFUned7Cd1WduR35l42J2ghA7SS8J+e80Ty5Bc11q6A==',
    '181d7c608da830efe255c510be09d2096a5599e7540e310adde70ab50f361ff4',
    'pASZ4pw5u3YEpZzlHmuTjqjwtKJYAInm8FBXZULqbzN/U5DV6dd6rcg=',
    'd251350014ec875cc4a093809abb639e05c97334ebf284222b0c82cfc6971df3',
    false, 'WORK', '2024-11-01', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE employee_num = '2402040015');

INSERT INTO employee (
    employee_num, employee_name, employee_password, phone, ext, email, address, birth_date,
    bank_name, account_number_enc, account_number_hash, resident_number_enc, resident_number_hash,
    initial_state, employ_state, hire_date, profile_id
)
SELECT
    '2402040016', '배대리', '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01000000016', '1016', 'daeri.bae@example.test', '서울시 테스트구 테스트로 16', '1993-12-12',
    '국민은행', 'QCxP2/svQ4ulFUned7Cd1WduR35l42J2ghA7SS8J+e80Ty5Bc11q6A==',
    '181d7c608da830efe255c510be09d2096a5599e7540e310adde70ab50f361ff4',
    'pASZ4pw5u3YEpZzlHmuTjqjwtKJYAInm8FBXZULqbzN/U5DV6dd6rcg=',
    'd251350014ec875cc4a093809abb639e05c97334ebf284222b0c82cfc6971df3',
    false, 'WORK', '2024-12-01', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM employee WHERE employee_num = '2402040016');

-- 모바일1팀 (4명)
INSERT INTO employee_hr_info (employee_id, org_id, effective_from, position_id, rank_id, job_id, employ_type, recruit_type, area_id)
SELECT e.employee_id, o.org_id, DATE '2024-03-01', p.position_id, r.rank_id, j.job_id, 'REGULAR', 'EXPERIENCED', a.area_id
FROM employee e
JOIN organization o ON o.org_name = '모바일1팀'
JOIN hr_position p ON p.position_name = '팀장'
JOIN hr_rank r ON r.rank_name = '과장'
JOIN job j ON j.job_name = '백엔드 개발자'
JOIN working_area a ON a.area_name = '서울 강남'
WHERE e.employee_num = '2402040006'
  AND NOT EXISTS (SELECT 1 FROM employee_hr_info h WHERE h.employee_id = e.employee_id);

INSERT INTO employee_hr_info (employee_id, org_id, effective_from, position_id, rank_id, job_id, employ_type, recruit_type, area_id)
SELECT e.employee_id, o.org_id, DATE '2024-03-01', p.position_id, r.rank_id, j.job_id, 'REGULAR', 'EXPERIENCED', a.area_id
FROM employee e
JOIN organization o ON o.org_name = '모바일1팀'
JOIN hr_position p ON p.position_name = '팀원'
JOIN hr_rank r ON r.rank_name = '대리'
JOIN job j ON j.job_name = '프론트엔드 개발자'
JOIN working_area a ON a.area_name = '서울 강남'
WHERE e.employee_num = '2402040003'
  AND NOT EXISTS (SELECT 1 FROM employee_hr_info h WHERE h.employee_id = e.employee_id);

INSERT INTO employee_hr_info (employee_id, org_id, effective_from, position_id, rank_id, job_id, employ_type, recruit_type, area_id)
SELECT e.employee_id, o.org_id, DATE '2024-04-01', p.position_id, r.rank_id, j.job_id, 'REGULAR', 'NEW', a.area_id
FROM employee e
JOIN organization o ON o.org_name = '모바일1팀'
JOIN hr_position p ON p.position_name = '팀원'
JOIN hr_rank r ON r.rank_name = '주임'
JOIN job j ON j.job_name = '백엔드 개발자'
JOIN working_area a ON a.area_name = '서울 강남'
WHERE e.employee_num = '2402040004'
  AND NOT EXISTS (SELECT 1 FROM employee_hr_info h WHERE h.employee_id = e.employee_id);

INSERT INTO employee_hr_info (employee_id, org_id, effective_from, position_id, rank_id, job_id, employ_type, recruit_type, area_id)
SELECT e.employee_id, o.org_id, DATE '2024-06-01', p.position_id, r.rank_id, j.job_id, 'REGULAR', 'NEW', a.area_id
FROM employee e
JOIN organization o ON o.org_name = '모바일1팀'
JOIN hr_position p ON p.position_name = '팀원'
JOIN hr_rank r ON r.rank_name = '사원'
JOIN job j ON j.job_name = 'QA 엔지니어'
JOIN working_area a ON a.area_name = '서울 강남'
WHERE e.employee_num = '2402040005'
  AND NOT EXISTS (SELECT 1 FROM employee_hr_info h WHERE h.employee_id = e.employee_id);

-- 인사팀 (4명)
INSERT INTO employee_hr_info (employee_id, org_id, effective_from, position_id, rank_id, job_id, employ_type, recruit_type, area_id)
SELECT e.employee_id, o.org_id, DATE '2022-09-01', p.position_id, r.rank_id, j.job_id, 'REGULAR', 'EXPERIENCED', a.area_id
FROM employee e
JOIN organization o ON o.org_name = '인사팀'
JOIN hr_position p ON p.position_name = '팀장'
JOIN hr_rank r ON r.rank_name = '과장'
JOIN job j ON j.job_name = '인사 평가'
JOIN working_area a ON a.area_name = '서울 강남'
WHERE e.employee_num = '2402040007'
  AND NOT EXISTS (SELECT 1 FROM employee_hr_info h WHERE h.employee_id = e.employee_id);

INSERT INTO employee_hr_info (employee_id, org_id, effective_from, position_id, rank_id, job_id, employ_type, recruit_type, area_id)
SELECT e.employee_id, o.org_id, DATE '2023-03-01', p.position_id, r.rank_id, j.job_id, 'REGULAR', 'EXPERIENCED', a.area_id
FROM employee e
JOIN organization o ON o.org_name = '인사팀'
JOIN hr_position p ON p.position_name = '팀원'
JOIN hr_rank r ON r.rank_name = '대리'
JOIN job j ON j.job_name = '인사 기획'
JOIN working_area a ON a.area_name = '서울 강남'
WHERE e.employee_num = '2402040008'
  AND NOT EXISTS (SELECT 1 FROM employee_hr_info h WHERE h.employee_id = e.employee_id);

INSERT INTO employee_hr_info (employee_id, org_id, effective_from, position_id, rank_id, job_id, employ_type, recruit_type, area_id)
SELECT e.employee_id, o.org_id, DATE '2024-07-01', p.position_id, r.rank_id, j.job_id, 'REGULAR', 'NEW', a.area_id
FROM employee e
JOIN organization o ON o.org_name = '인사팀'
JOIN hr_position p ON p.position_name = '팀원'
JOIN hr_rank r ON r.rank_name = '사원'
JOIN job j ON j.job_name = '인사 운영'
JOIN working_area a ON a.area_name = '서울 강남'
WHERE e.employee_num = '2402040009'
  AND NOT EXISTS (SELECT 1 FROM employee_hr_info h WHERE h.employee_id = e.employee_id);

INSERT INTO employee_hr_info (employee_id, org_id, effective_from, position_id, rank_id, job_id, employ_type, recruit_type, area_id)
SELECT e.employee_id, o.org_id, DATE '2024-09-01', p.position_id, r.rank_id, j.job_id, 'REGULAR', 'NEW', a.area_id
FROM employee e
JOIN organization o ON o.org_name = '인사팀'
JOIN hr_position p ON p.position_name = '팀원'
JOIN hr_rank r ON r.rank_name = '사원'
JOIN job j ON j.job_name = '채용 운영'
JOIN working_area a ON a.area_name = '서울 강남'
WHERE e.employee_num = '2402040010'
  AND NOT EXISTS (SELECT 1 FROM employee_hr_info h WHERE h.employee_id = e.employee_id);

-- 기술연구소 직속 (1명)
INSERT INTO employee_hr_info (employee_id, org_id, effective_from, position_id, rank_id, job_id, employ_type, recruit_type, area_id)
SELECT e.employee_id, o.org_id, DATE '2020-03-01', p.position_id, r.rank_id, j.job_id, 'REGULAR', 'EXPERIENCED', a.area_id
FROM employee e
JOIN organization o ON o.org_name = '기술연구소'
JOIN hr_position p ON p.position_name = '본부장'
JOIN hr_rank r ON r.rank_name = '과장'
JOIN job j ON j.job_name = '백엔드 개발자'
JOIN working_area a ON a.area_name = '서울 강남'
WHERE e.employee_num = '2402040011'
  AND NOT EXISTS (SELECT 1 FROM employee_hr_info h WHERE h.employee_id = e.employee_id);

-- 모바일1팀 정렬 검증용 추가 인원 (팀원: 과장 > 대리 > 주임 > 사원)
INSERT INTO employee_hr_info (employee_id, org_id, effective_from, position_id, rank_id, job_id, employ_type, recruit_type, area_id)
SELECT e.employee_id, o.org_id, DATE '2023-05-01', p.position_id, r.rank_id, j.job_id, 'REGULAR', 'EXPERIENCED', a.area_id
FROM employee e
JOIN organization o ON o.org_name = '모바일1팀'
JOIN hr_position p ON p.position_name = '팀원'
JOIN hr_rank r ON r.rank_name = '과장'
JOIN job j ON j.job_name = '백엔드 개발자'
JOIN working_area a ON a.area_name = '서울 강남'
WHERE e.employee_num = '2402040012'
  AND NOT EXISTS (SELECT 1 FROM employee_hr_info h WHERE h.employee_id = e.employee_id);

INSERT INTO employee_hr_info (employee_id, org_id, effective_from, position_id, rank_id, job_id, employ_type, recruit_type, area_id)
SELECT e.employee_id, o.org_id, DATE '2023-08-01', p.position_id, r.rank_id, j.job_id, 'REGULAR', 'EXPERIENCED', a.area_id
FROM employee e
JOIN organization o ON o.org_name = '모바일1팀'
JOIN hr_position p ON p.position_name = '팀원'
JOIN hr_rank r ON r.rank_name = '대리'
JOIN job j ON j.job_name = '프론트엔드 개발자'
JOIN working_area a ON a.area_name = '서울 강남'
WHERE e.employee_num = '2402040013'
  AND NOT EXISTS (SELECT 1 FROM employee_hr_info h WHERE h.employee_id = e.employee_id);

INSERT INTO employee_hr_info (employee_id, org_id, effective_from, position_id, rank_id, job_id, employ_type, recruit_type, area_id)
SELECT e.employee_id, o.org_id, DATE '2024-01-01', p.position_id, r.rank_id, j.job_id, 'REGULAR', 'NEW', a.area_id
FROM employee e
JOIN organization o ON o.org_name = '모바일1팀'
JOIN hr_position p ON p.position_name = '팀원'
JOIN hr_rank r ON r.rank_name = '주임'
JOIN job j ON j.job_name = 'QA 엔지니어'
JOIN working_area a ON a.area_name = '서울 강남'
WHERE e.employee_num = '2402040014'
  AND NOT EXISTS (SELECT 1 FROM employee_hr_info h WHERE h.employee_id = e.employee_id);

INSERT INTO employee_hr_info (employee_id, org_id, effective_from, position_id, rank_id, job_id, employ_type, recruit_type, area_id)
SELECT e.employee_id, o.org_id, DATE '2024-11-01', p.position_id, r.rank_id, j.job_id, 'REGULAR', 'NEW', a.area_id
FROM employee e
JOIN organization o ON o.org_name = '모바일1팀'
JOIN hr_position p ON p.position_name = '팀원'
JOIN hr_rank r ON r.rank_name = '사원'
JOIN job j ON j.job_name = '백엔드 개발자'
JOIN working_area a ON a.area_name = '서울 강남'
WHERE e.employee_num = '2402040015'
  AND NOT EXISTS (SELECT 1 FROM employee_hr_info h WHERE h.employee_id = e.employee_id);

INSERT INTO employee_hr_info (employee_id, org_id, effective_from, position_id, rank_id, job_id, employ_type, recruit_type, area_id)
SELECT e.employee_id, o.org_id, DATE '2024-12-01', p.position_id, r.rank_id, j.job_id, 'REGULAR', 'EXPERIENCED', a.area_id
FROM employee e
JOIN organization o ON o.org_name = '모바일1팀'
JOIN hr_position p ON p.position_name = '팀원'
JOIN hr_rank r ON r.rank_name = '대리'
JOIN job j ON j.job_name = '백엔드 개발자'
JOIN working_area a ON a.area_name = '서울 강남'
WHERE e.employee_num = '2402040016'
  AND NOT EXISTS (SELECT 1 FROM employee_hr_info h WHERE h.employee_id = e.employee_id);

-- 조직도 데모 계정 로그인 가능하도록 role/password_history 추가
INSERT INTO employee_role (employee_id, role_id)
SELECT e.employee_id, r.role_id
FROM employee e
JOIN role r ON r.role_code = 'EVALUATEE'
WHERE e.employee_num IN ('2402040003','2402040004','2402040005','2402040006','2402040007','2402040008','2402040009','2402040010','2402040011')
  AND NOT EXISTS (
      SELECT 1
      FROM employee_role er
      WHERE er.employee_id = e.employee_id
        AND er.role_id = r.role_id
  );

INSERT INTO employee_role (employee_id, role_id)
SELECT e.employee_id, r.role_id
FROM employee e
JOIN role r ON r.role_code = 'EVALUATEE'
WHERE e.employee_num IN ('2402040012','2402040013','2402040014','2402040015')
  AND NOT EXISTS (
      SELECT 1
      FROM employee_role er
      WHERE er.employee_id = e.employee_id
        AND er.role_id = r.role_id
  );

INSERT INTO employee_role (employee_id, role_id)
SELECT e.employee_id, r.role_id
FROM employee e
JOIN role r ON r.role_code = 'EVALUATOR'
WHERE e.employee_num = '2402040012'
  AND NOT EXISTS (
      SELECT 1
      FROM employee_role er
      WHERE er.employee_id = e.employee_id
        AND er.role_id = r.role_id
  );

INSERT INTO employee_role (employee_id, role_id)
SELECT e.employee_id, r.role_id
FROM employee e
JOIN role r ON r.role_code = 'EVALUATEE'
WHERE e.employee_num = '2402040016'
  AND NOT EXISTS (
      SELECT 1
      FROM employee_role er
      WHERE er.employee_id = e.employee_id
        AND er.role_id = r.role_id
  );

INSERT INTO password_history (employee_id, password_hash, change_at)
SELECT e.employee_id, '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy', NOW()
FROM employee e
WHERE e.employee_num IN ('2402040003','2402040004','2402040005','2402040006','2402040007','2402040008','2402040009','2402040010','2402040011')
  AND NOT EXISTS (
      SELECT 1
      FROM password_history ph
      WHERE ph.employee_id = e.employee_id
  );

INSERT INTO password_history (employee_id, password_hash, change_at)
SELECT e.employee_id, '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy', NOW()
FROM employee e
WHERE e.employee_num IN ('2402040012','2402040013','2402040014','2402040015')
  AND NOT EXISTS (
      SELECT 1
      FROM password_history ph
      WHERE ph.employee_id = e.employee_id
  );

INSERT INTO password_history (employee_id, password_hash, change_at)
SELECT e.employee_id, '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy', NOW()
FROM employee e
WHERE e.employee_num = '2402040016'
  AND NOT EXISTS (
      SELECT 1
      FROM password_history ph
      WHERE ph.employee_id = e.employee_id
  );

-- 증명서 정책 더미
INSERT INTO policy (policy_id, policy_type, policy_title, created_at, is_active)
SELECT 1, 'CERTIFICATE', '증명서 발급 정책', NOW(), TRUE
WHERE NOT EXISTS (SELECT 1 FROM policy WHERE policy_id = 1);

INSERT INTO policy_version (
    policy_version_id,
    policy_id,
    version_no,
    content,
    change_summary,
    employee_id,
    changed_at,
    effective_from
)
SELECT
    1,
    1,
    1,
    '1. 재직증명서는 본인 계정에서만 신청 가능합니다.\n2. 발급 요청 시 즉시 문서가 생성됩니다.\n3. 발급 이력에서 재다운로드 가능합니다.\n4. 허위 용도 사용 시 사내 규정에 따라 제한될 수 있습니다.',
    '초기 정책 등록',
    e.employee_id,
    NOW(),
    CURDATE()
FROM employee e
WHERE e.employee_num = '2402040001'
  AND NOT EXISTS (SELECT 1 FROM policy_version WHERE policy_version_id = 1);

-- 증명서 발급 이력 더미
INSERT INTO hr_file (hr_file_id, file_key, file_url, file_title)
SELECT
    301,
    'hr/certificate/1/employment_ko_2402040001_20260205.pdf',
    'https://cdn.rhight.local/certificate/employment_ko_2402040001_20260205.pdf',
    'employment_ko_2402040001_20260205.pdf'
WHERE NOT EXISTS (SELECT 1 FROM hr_file WHERE hr_file_id = 301);
