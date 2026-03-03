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
INSERT INTO `position` (position_name)
SELECT '팀장'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `position` WHERE position_name = '팀장'
);

INSERT INTO `position` (position_name)
SELECT '팀원'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `position` WHERE position_name = '팀원'
);

INSERT INTO `rank` (rank_name, rank_no)
SELECT '과장', 4
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `rank` WHERE rank_name = '과장'
);

INSERT INTO `rank` (rank_name, rank_no)
SELECT '대리', 3
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `rank` WHERE rank_name = '대리'
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
-- password hash = Spring!123
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
    profile_id
)
SELECT
    '2402040001',
    '김관리',
    '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01012345678',
    '1001',
    'admin.user@rhight.co.kr',
    '서울 강남구 테헤란로 1',
    '1990-01-02',
    '국민은행',
    TO_BASE64('110123456780'),
    SHA2(CONCAT('110123456780', 'l6WrJOYmY0JuxbStYlTwIpdSr57i0uAOyUvHaNdQKB0='), 256),
    TO_BASE64('9001021234567'),
    SHA2(CONCAT('9001021234567', 'l6WrJOYmY0JuxbStYlTwIpdSr57i0uAOyUvHaNdQKB0='), 256),
    false,
    'WORK',
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
    profile_id
)
SELECT
    '2402040002',
    '박일반',
    '$2a$10$5hZDaK2H9brQmKbMa5ZoEuaJ7XfGZgQsOOrBOsPxXUXvYwtD0.8Sy',
    '01023456789',
    '1002',
    'normal.user@rhight.co.kr',
    '서울 서초구 서초대로 2',
    '1992-07-11',
    '국민은행',
    TO_BASE64('110123456789'),
    SHA2(CONCAT('110123456789', 'l6WrJOYmY0JuxbStYlTwIpdSr57i0uAOyUvHaNdQKB0='), 256),
    TO_BASE64('9207111234567'),
    SHA2(CONCAT('9207111234567', 'l6WrJOYmY0JuxbStYlTwIpdSr57i0uAOyUvHaNdQKB0='), 256),
    false,
    'WORK',
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
JOIN `position` p ON p.position_name = '팀장'
JOIN `rank` r ON r.rank_name = '과장'
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
JOIN `position` p ON p.position_name = '팀원'
JOIN `rank` r ON r.rank_name = '대리'
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
