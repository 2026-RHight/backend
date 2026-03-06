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
INSERT INTO leave_balance (employee_id, total_annual_leave) VALUES (1, 15.0);
INSERT INTO leave_balance (employee_id, total_annual_leave) VALUES (2, 20.0);

-- 출퇴근 기록
INSERT INTO attendance_record (employee_id, work_date, check_in_time, check_out_time, status, tardy_reason, modify_reason)
VALUES (1, '2026-03-04', '2026-03-04 08:50:00', '2026-03-04 18:05:00', 'NORMAL', NULL, NULL);

INSERT INTO attendance_record (employee_id, work_date, check_in_time, check_out_time, status, tardy_reason, modify_reason)
VALUES (2, '2026-03-04', '2026-03-04 09:15:00', NULL, 'TARDY', '지하철 연착', NULL);


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
INSERT INTO hr_event (
    employee_id,
    event_type,
    event_title,
    requested_at,
    approved_at,
    effective_from,
    effective_to,
    excuse,
    before_change,
    after_change
)
SELECT
    e.employee_id,
    'PROMOTION',
    '직급 변경',
    '2025-12-20 09:10:00',
    '2025-12-27 14:30:00',
    DATE '2026-01-01',
    NULL,
    '정기 승진',
    JSON_OBJECT('rankName', '주임'),
    JSON_OBJECT('rankName', '대리')
FROM employee e
WHERE e.employee_num = '2402040001'
  AND NOT EXISTS (
      SELECT 1
      FROM hr_event h
      WHERE h.employee_id = e.employee_id
        AND h.event_type = 'PROMOTION'
        AND h.effective_from = DATE '2026-01-01'
  );

INSERT INTO hr_event (
    employee_id,
    event_type,
    event_title,
    requested_at,
    approved_at,
    effective_from,
    effective_to,
    excuse,
    before_change,
    after_change
)
SELECT
    e.employee_id,
    'TRANSFER',
    '부서 이동',
    '2025-01-18 10:00:00',
    '2025-01-25 16:10:00',
    DATE '2025-02-02',
    NULL,
    '프로젝트 조직 개편',
    JSON_OBJECT('orgName', '모바일3팀'),
    JSON_OBJECT('orgName', '모바일1팀')
FROM employee e
WHERE e.employee_num = '2402040001'
  AND NOT EXISTS (
      SELECT 1
      FROM hr_event h
      WHERE h.employee_id = e.employee_id
        AND h.event_type = 'TRANSFER'
        AND h.effective_from = DATE '2025-02-02'
  );

INSERT INTO hr_event (
    employee_id,
    event_type,
    event_title,
    requested_at,
    approved_at,
    effective_from,
    effective_to,
    excuse,
    before_change,
    after_change
)
SELECT
    e.employee_id,
    'STATE_CHANGE',
    '재직 상태 변경',
    '2024-07-23 10:40:00',
    '2024-07-29 18:20:00',
    DATE '2024-08-01',
    DATE '2024-11-30',
    '육아 휴직 신청',
    JSON_OBJECT('employeeState', 'WORK'),
    JSON_OBJECT('employeeState', 'LEAVE')
FROM employee e
WHERE e.employee_num = '2402040001'
  AND NOT EXISTS (
      SELECT 1
      FROM hr_event h
      WHERE h.employee_id = e.employee_id
        AND h.event_type = 'STATE_CHANGE'
        AND h.effective_from = DATE '2024-08-01'
  );

INSERT INTO hr_event (
    employee_id,
    event_type,
    event_title,
    requested_at,
    approved_at,
    effective_from,
    effective_to,
    excuse,
    before_change,
    after_change
)
SELECT
    e.employee_id,
    'STATE_CHANGE',
    '재직 상태 변경',
    '2024-11-20 11:30:00',
    '2024-11-27 09:50:00',
    DATE '2024-12-01',
    NULL,
    '복직 승인',
    JSON_OBJECT('employeeState', 'LEAVE'),
    JSON_OBJECT('employeeState', 'WORK')
FROM employee e
WHERE e.employee_num = '2402040001'
  AND NOT EXISTS (
      SELECT 1
      FROM hr_event h
      WHERE h.employee_id = e.employee_id
        AND h.event_type = 'STATE_CHANGE'
        AND h.effective_from = DATE '2024-12-01'
  );
