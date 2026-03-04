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
