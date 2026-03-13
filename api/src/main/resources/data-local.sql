-- RHIGHT backend seed data (MySQL 8+)
-- Generated only from schema.sql and the provided requirements.
-- Skills/career evidence data is intentionally omitted.

START TRANSACTION;

-- ---------------------------------------------------------------------------
-- Organization seed
-- ---------------------------------------------------------------------------
INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9001, 'RHIGHT', 'COMPANY', NULL, 1, 1, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9001);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9101, '경영지원본부', 'HEADQUARTER', 9001, 2, 1, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9101);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9102, '제품개발본부', 'HEADQUARTER', 9001, 2, 2, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9102);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9103, '영업본부', 'HEADQUARTER', 9001, 2, 3, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9103);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9201, '인사센터', 'CENTER', 9101, 3, 1, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9201);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9202, '재무센터', 'CENTER', 9101, 3, 2, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9202);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9203, '플랫폼실', 'DIVISION', 9102, 3, 1, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9203);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9204, '프로덕트실', 'DIVISION', 9102, 3, 2, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9204);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9205, '국내영업센터', 'CENTER', 9103, 3, 1, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9205);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9301, '인사팀', 'TEAM', 9201, 4, 1, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9301);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9302, '총무팀', 'TEAM', 9201, 4, 2, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9302);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9303, '재무팀', 'TEAM', 9202, 4, 1, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9303);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9304, '백엔드팀', 'TEAM', 9203, 4, 1, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9304);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9305, '프론트엔드팀', 'TEAM', 9203, 4, 2, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9305);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9306, '모바일팀', 'TEAM', 9204, 4, 1, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9306);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9307, 'QA팀', 'TEAM', 9204, 4, 2, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9307);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9308, '영업1팀', 'TEAM', 9205, 4, 1, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9308);

INSERT INTO organization (org_id, org_name, org_type, parent_org_id, org_level, sort_order, is_active)
SELECT 9309, '영업2팀', 'TEAM', 9205, 4, 2, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM organization WHERE org_id = 9309);

-- 자기 자신(depth 0) 포함 + 상위/하위 전체 전이관계 저장
INSERT INTO org_closure (ancestor_org_id, descendant_org_id, depth, sort_path)
WITH RECURSIVE seeded_tree AS (
    SELECT
        o.org_id,
        o.parent_org_id,
        CAST(CONCAT(LPAD(COALESCE(o.sort_order, 0), 4, '0'), '-', LPAD(o.org_id, 10, '0')) AS CHAR(2000)) AS sort_path
    FROM organization o
    WHERE o.org_id = 9001
      AND o.is_active = TRUE

    UNION ALL

    SELECT
        child.org_id,
        child.parent_org_id,
        CAST(
                CONCAT(
                        parent.sort_path,
                        '/',
                        LPAD(COALESCE(child.sort_order, 0), 4, '0'),
                        '-',
                        LPAD(child.org_id, 10, '0')
                ) AS CHAR(2000)
        ) AS sort_path
    FROM organization child
             JOIN seeded_tree parent ON child.parent_org_id = parent.org_id
    WHERE child.is_active = TRUE
),
               closure AS (
                   SELECT
                       st.org_id AS ancestor_org_id,
                       st.org_id AS descendant_org_id,
                       0 AS depth
                   FROM seeded_tree st

                   UNION ALL

                   SELECT
                       c.ancestor_org_id,
                       child.org_id AS descendant_org_id,
                       c.depth + 1 AS depth
                   FROM closure c
                            JOIN organization child ON child.parent_org_id = c.descendant_org_id
                   WHERE child.is_active = TRUE
               )
SELECT
    c.ancestor_org_id,
    c.descendant_org_id,
    c.depth,
    st.sort_path
FROM closure c
         JOIN seeded_tree st ON st.org_id = c.descendant_org_id
         LEFT JOIN org_closure existing
                   ON existing.ancestor_org_id = c.ancestor_org_id
                       AND existing.descendant_org_id = c.descendant_org_id
WHERE existing.ancestor_org_id IS NULL;

-- ---------------------------------------------------------------------------
-- Master data seed
-- ---------------------------------------------------------------------------
INSERT INTO hr_rank (rank_name, rank_no)
SELECT seed.rank_name, seed.rank_no
FROM (
         SELECT '사원' AS rank_name, 1 AS rank_no
         UNION ALL SELECT '주임', 2
         UNION ALL SELECT '대리', 3
         UNION ALL SELECT '과장', 4
         UNION ALL SELECT '차장', 5
         UNION ALL SELECT '부장', 6
         UNION ALL SELECT '이사', 7
         UNION ALL SELECT '상무', 8
         UNION ALL SELECT '전무', 9
         UNION ALL SELECT '부사장', 10
         UNION ALL SELECT '사장', 11
     ) seed
         LEFT JOIN hr_rank r ON r.rank_name = seed.rank_name
WHERE r.rank_id IS NULL;

INSERT INTO hr_position (position_name)
SELECT seed.position_name
FROM (
         SELECT '팀원' AS position_name
         UNION ALL SELECT '팀장'
         UNION ALL SELECT '파트장'
         UNION ALL SELECT '실장'
         UNION ALL SELECT '부서장'
         UNION ALL SELECT '본부장'
         UNION ALL SELECT '센터장'
         UNION ALL SELECT 'CEO'
     ) seed
         LEFT JOIN hr_position p ON p.position_name = seed.position_name
WHERE p.position_id IS NULL;

INSERT INTO job (job_name)
SELECT seed.job_name
FROM (
         SELECT '경영지원' AS job_name
         UNION ALL SELECT '인사'
         UNION ALL SELECT '재무'
         UNION ALL SELECT '백엔드 개발'
         UNION ALL SELECT '프론트엔드 개발'
         UNION ALL SELECT '모바일 개발'
         UNION ALL SELECT 'QA'
         UNION ALL SELECT '영업'
     ) seed
         LEFT JOIN job j ON j.job_name = seed.job_name
WHERE j.job_id IS NULL;

INSERT INTO working_area (area_name)
SELECT seed.area_name
FROM (
         SELECT '서울 본사' AS area_name
         UNION ALL SELECT '판교 캠퍼스'
         UNION ALL SELECT '부산 지사'
         UNION ALL SELECT '원격'
     ) seed
         LEFT JOIN working_area a ON a.area_name = seed.area_name
WHERE a.area_id IS NULL;

INSERT INTO role (role_code, role_name, description)
SELECT seed.role_code, seed.role_name, seed.description
FROM (
         SELECT 'EVALUATOR' AS role_code, '평가자' AS role_name, '조직 평가 권한' AS description
         UNION ALL SELECT 'EVALUATEE', '피평가자', '기본 평가 대상 권한'
         UNION ALL SELECT 'HR_ADMIN_MASTER', '인사관리자-총괄', '인사 관리 총괄 권한'
         UNION ALL SELECT 'HR_ADMIN_PAYROLL', '인사관리자-급여', '급여 관리 권한'
         UNION ALL SELECT 'HR_ADMIN_BASIC', '인사관리자-기본', '기본 인사정보 관리 권한'
     ) seed
         LEFT JOIN role r ON r.role_code = seed.role_code
WHERE r.role_id IS NULL;

-- ---------------------------------------------------------------------------
-- 100 employees (profile_id uses existing basic profile file)
-- ---------------------------------------------------------------------------
INSERT INTO hr_file (file_key, file_url, file_title)
SELECT 'hr/profile/basicprofile.webp',
       'http://beyond21.iptime.org:2103/rhight/hr/profile/basicprofile.webp',
       'basicprofile.webp'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM hr_file
    WHERE file_key = 'hr/profile/basicprofile.webp'
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
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1
    FROM seq
    WHERE n < 100
),
               employee_seed AS (
                   SELECT
                       n,
                       DATE_ADD(DATE('2025-02-12'), INTERVAL FLOOR((n - 1) / 10) DAY) AS hire_date,
                       MOD(n - 1, 10) + 1 AS day_seq
                   FROM seq
               ),
               sensitive_seed AS (
                   SELECT 1 AS n, '1f8bWU52stEiAQrUpZpFE1/Ht/hCsIZeYSzn27iaeb+cz3eQpVvSu94LZA==' AS account_number_enc, '9d26e3ef6db6ce4644cfeb407d4b5b14efe22feb458406cf834d1d2eb2503a0c' AS account_number_hash, '3oz2LQ3k4BkuPvYQv72DDjY0I1li7ZMmUcXI5HN+mZFeioYLR6R8tk8w' AS resident_number_enc, '71a018b398080741cdce505498766439574e44f38b21864fb0eb67617fc71f7e' AS resident_number_hash UNION ALL
                   SELECT 2 AS n, 'b+FPdUshl9sygSx3ElOaaaNdyPvr5dHNkV1knCOoZ7jHTE2VxT09I4Zlqw==' AS account_number_enc, 'fe99e28ce9a7aa6fb52eea2ea1b6623ea88c370ee5577959054633a88fc237d2' AS account_number_hash, '6akjt5CMq59Lpyi4EqVTrxTBioWOs3cfYsa+4+RPrxydK0UV24cJwbM9' AS resident_number_enc, '92d477d43c99196dad06014f0666c5ca4ac23492eba878dd170b929d9a7b3ecd' AS resident_number_hash UNION ALL
                   SELECT 3 AS n, 'i7nESualDpBx3Yt2Lsk1pttTCigPn5/Pbfjl44FFSPB6JFGVEiumb1ajUw==' AS account_number_enc, 'ed665032c44857126e8b009c6969b6266aaac498fd5777d0ea5cbacbe2d72b3f' AS account_number_hash, 'Ew+GA8ufWrr0MKPQuJvJJyZUhNhZGT3IEDg6O/H90kXlIMrP8YdMlFaY' AS resident_number_enc, 'cc5c8cc0fb716b72deb35b62569a9d82cb8a85ea282638358bd89dff6a7b2dbf' AS resident_number_hash UNION ALL
                   SELECT 4 AS n, 'Axap8+Lzc5fKvdPU36KKD4lxMKlhjlUqhUdCZs4+zqF29q8IVk5ymD2FyA==' AS account_number_enc, '9e407cef9837111e313bea8fa64130524f2ff409af9fe451970063b6f583c0c9' AS account_number_hash, 'z1JLWA+0xMc80xq7pqvn75DIwnVlV4r7YOX4iptTwQw2DVu2S6UED7v3' AS resident_number_enc, '3a9062d559e294ba26565afc06ffbe7eb70776236a44bbecc53f9cb6540bbcf8' AS resident_number_hash UNION ALL
                   SELECT 5 AS n, 'I5V4ugz3MXNOq2jpdt1q50zUxA3B0JBdbp632HMt8M+K6wik/ErKlQGFDA==' AS account_number_enc, 'f9cf7cd481ae4a239a7533c03d289b8563b53661b80820396de6fb3e66ade34b' AS account_number_hash, '5uLoNTu9dzO+yB4V/M2UWqTNspdA8BiZ+Y+1dlxkCdTy0K/Xk6t1LTTk' AS resident_number_enc, '4443182e056fa28da7785723e582a7ee6059077f961302bcad73b455ff2492e0' AS resident_number_hash UNION ALL
                   SELECT 6 AS n, '2G8Z1qIfeINvm6SfeFREsQSx0Lq4SyiTOBeFqnOuICGv6K8YOtNgHSJpQw==' AS account_number_enc, '8987cf0fe9cae3e7e6cd70dd9a27cc98985fe326d0dd8c0a69920ca080dd682b' AS account_number_hash, '3U7QrVpHV5VNw7XbdfFKIiEoRAuIj8b3RNZApAuWIFJSwxoBHX4mluZ1' AS resident_number_enc, '1a34b48ce1f2c59fa8474d9e8a4e2c1fa412f34ea33fcea0044c220e23a528bb' AS resident_number_hash UNION ALL
                   SELECT 7 AS n, '1nzVBJT27VDNEnsmOvzw5Zsm7BdnvxtfbkD2FlYY7mb67FJ3qlDWu8a4Zg==' AS account_number_enc, '9c0c789fcfddafc561cdf3af562b1835b17a5a2d56b373ae2c388eef7381bcdc' AS account_number_hash, 'c/w6b6shEpQbZiBI1xIU18z6o02Ikv47dd8oYzJaNKwJGX6QRYOgDVi8' AS resident_number_enc, 'cf698cd2d22eedfcfbadeac1f9df1723a15b192b5f3ea75ca808dafb55ac089f' AS resident_number_hash UNION ALL
                   SELECT 8 AS n, 'JRVLKiWAG/V7v1H+o8VcfLF4hEbOSsj8B8Nv3eq75e+hGjhodM1JjR4GwQ==' AS account_number_enc, 'd95c410d392d4dc89cb7c142423c55781af7b06015d7e6094c9ea7b07ebdd873' AS account_number_hash, 'TJugZXbN2r4ApDD7aJ439Q5pRfyhSbZU0UnoWr67WoySWCc2/r59EfSE' AS resident_number_enc, 'd6846683d21bc0fe2b72826d24000046549771a708330d2c66331a3c9a5d4d51' AS resident_number_hash UNION ALL
                   SELECT 9 AS n, '1MVInLMci+165rzGZjrSQ1IGxjprnGe09av0EFQDYY3DOQuwQHIzQAICXw==' AS account_number_enc, '8420bcbf1e07f5ffb0ae09417412b8087dded8d2c944d2f7bb28bd5337a2608b' AS account_number_hash, 'K3LAVHEfkflgD2dDO7wN9YO5GJbbuE33ya1YOjB5eN98W0hYFswF9ilG' AS resident_number_enc, 'fe6a3029966c19c24e1f502f72bd39099733ae31e17371350b99ba5f7ee2c6fc' AS resident_number_hash UNION ALL
                   SELECT 10 AS n, 'tbddJFYrDzCc4Bb572SBsuqdqd+Tsm74SaS5R9qdfPy74reB6Aebh870sg==' AS account_number_enc, '418e11d656ce83ec2e7ef8d9fb7ac073f50de22cbabba2241a22d50fa381dc01' AS account_number_hash, 'lNJUe7RvAAjAri5Rlt/M2eZLbSUqI4cvb3vd/eebKom02+imn/5ejQo9' AS resident_number_enc, '9961341ca5db8dc933dbd4fd2d1eefd80338337b68d189779fa375fb6d868199' AS resident_number_hash UNION ALL
                   SELECT 11 AS n, 'I/N24+og0bsaJKDDof5JdXFH8eKOadHw3k7ex+s0giUUroEqQk36j8ZJPA==' AS account_number_enc, '5d80a808c2062c0a1f8c1afcd2870643c0c25f99ba8a7c616a3d7230dc78266c' AS account_number_hash, '9xqXeDObHNx1DIfTj72keotd3jIV9Vbe+AX2Teb7m+eoNzFbaopVNEra' AS resident_number_enc, '66138001514eee8dae7b9fc097f21012fea2ebedbd9e4d0579c224824318e91d' AS resident_number_hash UNION ALL
                   SELECT 12 AS n, 'OP3q4gGaq2jxiAVTOaXXumb0Hl4Vk9Ui0NbGWnA04HkmdgsNxwYjUYqutA==' AS account_number_enc, '42b7b4f2c4b596bee78269700219ff8210d7ddf0a7f32bae8b0d4cb9e23e35a8' AS account_number_hash, 'HnrukZHuBieseek/Mn+QPfhx0yDAcDakhSc6TRnuFDuBsk+HiJLLbhQV' AS resident_number_enc, 'b9ec54c88593053ed636590ee038e57299e52bd8743600e8d1fcc0dfe091d698' AS resident_number_hash UNION ALL
                   SELECT 13 AS n, 'LcnuYURLU1IW5ZwE4EgGs3yvxRzY5sTXP+ZxWlbQwQE4ZNBvYWNg71YGWw==' AS account_number_enc, 'e4fed0387bf157619c8e971af3ef065ec8c8a5302fddf32a7aa1d515933a4c8d' AS account_number_hash, '+UnNJhBPDrKpNvgw+hGswrGtLYP/QPynlrTaM7dKRxtkLHSSba3NrZ5Y' AS resident_number_enc, 'ae679ce38cb1d8173dfca93a7279fe6f535fd7163db67b1dda3e3521857c50b8' AS resident_number_hash UNION ALL
                   SELECT 14 AS n, 'Wn7ibsM47KWQAov2PtOQWgCi9XBOljdg/K4/lOEvzbuidS5W0TkUIfcfcw==' AS account_number_enc, '0d01e9c32762a8dc32dc4fc2b59d90322258d89e26c4223e8ae0cf474a8975a3' AS account_number_hash, '6by+UMm6VciBMEjMC+XIcrx2WiNi9jRcsbKJEk4MJQ2g9UStX2BZvMqM' AS resident_number_enc, 'a3af614a74fa5f3f5189f5b2d5b9e4e1c17e5d2af4560a7eabf1ac96683d9cd7' AS resident_number_hash UNION ALL
                   SELECT 15 AS n, 'd2UtzR4Yk2tMXBmCEPSKKP82K0nV8x9bwQatT52oUj4U6L6vYJkVSqkRWw==' AS account_number_enc, '1a1233585df3796abc561d3384971b2f8d5653579cc78669902fee986da58882' AS account_number_hash, 'z/ZG4NzTBDgVXvFvtuRgKqUvFzGdbcKnKr/nE1Y3dKZ3fFRiYEp0hYqf' AS resident_number_enc, '0ba4292633644bc5b728c943a221c6d4fb9fec75ec8d42b47d64859c14ec344c' AS resident_number_hash UNION ALL
                   SELECT 16 AS n, 'CtfwyG9AxQGXXODqqOPsCTARZQi3+sPeIlT5gFnNlHIFVWx1HbnEOypMqg==' AS account_number_enc, '420767cad39a4d05e37fd6b129bfb5a6f08594655c8cb130e8caf6097a79f424' AS account_number_hash, 'pF9sFP+R/bhuCnDRmsRfKPDq5HGvWhaOmW6MnEoraj4QlWkdi8HgdDla' AS resident_number_enc, '8e6aa5d0df0c229f90e6a1a0a4c9b98e0797c47b92a6ef5ebdb24a2b29f04cb2' AS resident_number_hash UNION ALL
                   SELECT 17 AS n, 'OuBbsM+hNREiY28VOMk6kmut8yMEtx8TcPjp+09Jzo01pyU0WYN3kZ+fkQ==' AS account_number_enc, 'f5f8cac49fb355f9581947c867794cf5eaa8fe2fc8ffbf1910be88daeb7b83d8' AS account_number_hash, 'c4EKzeKJchaYYFaXUuvGWCkD8WSdL8rbyqQ2MuPABRrzwc3TgPuk+zzQ' AS resident_number_enc, '49730ad44ee3aa3657aceca46d5ba6df855a17a6a43237637ca05c13d73abb55' AS resident_number_hash UNION ALL
                   SELECT 18 AS n, 'Q5YXPGsWYvhKuI8PMzvdlAo51a2dhskM+S2bavMwhj2ndVWwRXW4rGwzwQ==' AS account_number_enc, 'a3fbb645b2874fcccd28d231d44b003c1841414c25c11fe1acfbffb589098f4d' AS account_number_hash, 'N1GPjtnug4F6jk7LovZ4gVwmvH2osFieAok3b38TwlGoVnKwzUa+xZfr' AS resident_number_enc, 'b8d438049914bdd19d5b19994d33bb982449743f37a82d4ee294b9d200f7cb2f' AS resident_number_hash UNION ALL
                   SELECT 19 AS n, 'EzurKVMsUwJ3P9hzRYSgrZJMvTPxEPgNnwuVXgMc0PsCwDvAYVWfNjJVXw==' AS account_number_enc, '2e9a051f9d77eda7b6bd3a3afebff7e340fe37bdcf71378896b2d1adb15fa0f6' AS account_number_hash, '9UABDAsjAxBpZnIGyKk1eQegyv1mBWuNScKZ41kXp9F3AFGaVA/ZXHfj' AS resident_number_enc, '9cdbdea8951ff907f2bb439b22327ebb06206bcbf2c7d345d9ea46478b8e0c0f' AS resident_number_hash UNION ALL
                   SELECT 20 AS n, 'uwK9cM9tmKvmtBWl41wPhT8dG43jqE2XPILXOH6l/9O276tpRbegJ0Nqag==' AS account_number_enc, '5680ad2a240075e02f53dabd331fc89cf3c7073a1d59f169b8370e4befdba801' AS account_number_hash, 'jWWmDG/Numg5+pwJpw/4XMrWXzKRxA2Z+jYxdPOkSr2J7ATx2UupWNYS' AS resident_number_enc, 'f06aad6720645e91dd4329d1c67f15b24218cdc9ee871ec7b1d1a68318e105f6' AS resident_number_hash UNION ALL
                   SELECT 21 AS n, 'tPE14LpPuQ3hzkyUlciSBfpZhC1gaRi2704JM7hIwfEO77advEAU4MTfcQ==' AS account_number_enc, '2304b630cb225c687911260a65aaf0f0605b87bb3537f46ddca425f7d4a57878' AS account_number_hash, 'BDDxMIRaxJAZTtr0R8GoGcQGKbxRnQo/9nGKUFTppC3js7HpLeCJpI1H' AS resident_number_enc, 'e0b769fa0816ed714aa2dfa4ac6d37bd1436a4b75335df97d0692e9bef858f73' AS resident_number_hash UNION ALL
                   SELECT 22 AS n, '4RfQu2+kEFM3XE7H+0USq8JCSeKz3zPs1vfsZzk3G3sBfFMtqN+wV6p3mA==' AS account_number_enc, 'b0151a3617d496db6a59efe702cf90e7688b9be194dd1403b3e6a769cfc4d591' AS account_number_hash, '/3OiY+bA+NR02jrhTo22Y9qTkdX4GlKSmsXXFRJ4jynr9DmuTVXFp712' AS resident_number_enc, '40ccf9881da7651b05126a235fe8d6cc935c583903f4aeef73767e015873ad13' AS resident_number_hash UNION ALL
                   SELECT 23 AS n, 'oWCqLyucHthphQzOs0PHQEABAH/x2/P4NnM97YEBkdhRYd8+BmILfvuDeA==' AS account_number_enc, '650c5e288bab529345e2bcc36e570144dbeeb86529251fe91d9d0b3df86a8d5a' AS account_number_hash, 'HMCTHUkZdHV5UsQeY1jT98p2ePDPPJgk6oiMlHXev6xSLZvb1wQzsTNt' AS resident_number_enc, 'fde03c71d99aa23d843192e62dcbf07107016ce8b291eb5592e997489de5a8be' AS resident_number_hash UNION ALL
                   SELECT 24 AS n, 'PkXQUlruOTgu2te3AS5i3UEQjADmJ/ZUTfNui3D83mJUjmlewKMVw97mkQ==' AS account_number_enc, '61fe9780f9a53d6ed18bfbac3363d61292c213f585f9729631f37a9874635707' AS account_number_hash, 'bfrYvQeTJ7/Obcru7L3N6yQ8xbfsESQFsCLs7buEqlc8PMl8gO4O5vIs' AS resident_number_enc, '12c940251116ccb1d7808ba0f8a9d85040826bb294bb0d2f45bcfcb114a037a0' AS resident_number_hash UNION ALL
                   SELECT 25 AS n, 'n4nTU7SewB6f9tY9jFCoe3ArwE4nZxeRi247SjaAr1j9qcdeLsJVkgAIjQ==' AS account_number_enc, '76f9960cc8c7177d7586cc105c9672897a5c0f021ac5db9df38b71576232021f' AS account_number_hash, 'a08BsISSdjTUDCz6gz7f5tqJSVGYZnZWFnDABJwuTnLmkuKcSznyKWyX' AS resident_number_enc, '9e756e6f91ec048369c8af11d2faf32c44f5164f7e9f7a252003e700886726f3' AS resident_number_hash UNION ALL
                   SELECT 26 AS n, 'OfYtvSUtu1PNpbnYdt1dEabSz0k9fKPItxHFK6RJBL6tc2LDGofQYiqLPA==' AS account_number_enc, '5ea3f25979d72ed5e20e2d3c7c255a37e89cfb32a5815469114338b4f7e2834e' AS account_number_hash, 'FR983a0+Nn/bWzysJPDp5/nT9BrSz1XRBFj9m5uxuXJ1JsQbrUYm+U+p' AS resident_number_enc, 'ed8b82b816bb0b59713405e756942c3bcd3ad9e255513a1749fbd64697faa6ab' AS resident_number_hash UNION ALL
                   SELECT 27 AS n, 'qr59dDPuSt5rvQtc27YvqdCZnBfR/dEa8b57eIwXJt5zvsBlJRNCkKSYlA==' AS account_number_enc, '09eb55635f3886a9192de4f25da36840ee591528b93dbdf52cec5f41cdc9b109' AS account_number_hash, '/Yy89Gn62BftrKqWjClLDaJqSUw6Bl+6/nhLri+Bo96H8JCqfYrOqeoA' AS resident_number_enc, '30bf014d2a42cf85eada96bff8d1fbc29ec1321f72fabd4dae962dff6424629b' AS resident_number_hash UNION ALL
                   SELECT 28 AS n, 'WVIaIeCgbEMCUpQxzcClmZJdHDs/nlkAY08zb3s229ZMAadYeLPTW5+s/A==' AS account_number_enc, '8f55c577114220be092014ad9370036e2253263a733532c52a8d5a2510522724' AS account_number_hash, '5pYlZg07nRlLNMLWgE2Hfn/7Lvan0+lyRFxLZOECjRAbCVni1YPHMioh' AS resident_number_enc, 'f2ebda850006c579f1a9c2b99b6e8d436762c5a8bf781c59e3d894654b764a0c' AS resident_number_hash UNION ALL
                   SELECT 29 AS n, 'ao64zjPhmDZdgBa0tIESEaW60GD32FBx4QrJIqeyz61F03spAaWDkPHJCQ==' AS account_number_enc, '89c51e78e538f9272c50fb1aadeeab0bb72248b0bf151a0f38163fc3bcc2c65e' AS account_number_hash, 'zyfW4i9iBBjrw16dUXcbdTU3icvPMrJZWE8ijgfW4eXxwkp+fZf3453v' AS resident_number_enc, '210da842c323eb21167bbf75cc62c17987c18b330f300fa76450ff076fd1e0c6' AS resident_number_hash UNION ALL
                   SELECT 30 AS n, 'yFpBmqTjANBcxoxkpD9uq19bDPw13RNK2WlIVURkk/FslNBPULcsUZUKlA==' AS account_number_enc, 'd578a76b24327c7a815f4e917495aaa3b6de6370f035d2cce0a382b51f7eb3b1' AS account_number_hash, '9ZLo8dTf4pO/hf1J3UBexEEXKTnnv/yEk1C6ra3L4ury9jX389pqN2g1' AS resident_number_enc, '0f07d21ce2dff64bba61dcc8a890da001b9b553d19d052647d8f59c2ad277e08' AS resident_number_hash UNION ALL
                   SELECT 31 AS n, 'BKj+yJizGLeQBOBGs2/ZJ/GKU2DjpizkciOqj2pr45HE+TLEp+1c7G2n1Q==' AS account_number_enc, 'e43d8ff2627ec28fe736d8b5ed733e0b72ec68d94c7e3d165755adb7e35c44cb' AS account_number_hash, '4SPl2DO/CfMpj8Z7IKmjsHr3++LPyGolAuYrLqfKAbMqVW/qOGbjHCmc' AS resident_number_enc, '7b5accbaf8663f5849849c26b863a30e123a6dd71ab6b5eb00cc54a2459986f7' AS resident_number_hash UNION ALL
                   SELECT 32 AS n, 'LN6GnK8bX4z/KsSmqVnDJ0aewVyQt/cEhjIhc5LbjvEMsgheTuixVxUSnQ==' AS account_number_enc, '84b5bf75985111f11e9e3cb67cbd2bdbda6bda45b6cb0d4cc8f3056aa7bfd2e8' AS account_number_hash, 't5jZ8GfvffYJaUTv/4bVH2sSCMr/NcTI/K2QmSbfFlRLXYGEWb1DHWn1' AS resident_number_enc, 'c810b55710527db12daf505b6f3f823df300a08fba5b270b5e1e61aafafd8ae8' AS resident_number_hash UNION ALL
                   SELECT 33 AS n, 'EWfQiyBAlLAP1eSmu9qXS+MLb+Qulz//vmhdCyoynsrU68v88syoVilLKA==' AS account_number_enc, 'a0fdaf2a1165d6a4be7e93ed695555d78961a0e6c2f26b0dbb1fd2375fac9f4c' AS account_number_hash, 'b7oiw+cPKU1xlAsvPpH9oCww5Iu7crGMzYZDIN45mUILr+2zX785Nirb' AS resident_number_enc, '968881c9388edca48c403e1e98d057bea11bb1193e2b924f6883be134b7c454b' AS resident_number_hash UNION ALL
                   SELECT 34 AS n, 'wYzO1wKZ8D5DMtuNIRW96ME0F6y8R4+C0zXsWNM2jEa2wrNbnzSk8XfhqA==' AS account_number_enc, '0d1c663d0e3a05cf90cc3bb664f07b2554f17321e478821a2e00e4f8cac72d54' AS account_number_hash, 'AgiygI6UPQKoWAopVrTNSSi6pNQ9G5FHm7zfXtgkn2VNF4EPaFyTfejT' AS resident_number_enc, 'a98a79cb6cf4ba9f0f4f67430e03d3d8be24b54bfb1de71969b31cd220692cae' AS resident_number_hash UNION ALL
                   SELECT 35 AS n, 'N98c5y8vPlUOWVlsMyENB0hBqT7oNirCswNgFnILaBQdA2wQV3zLiKN0XQ==' AS account_number_enc, 'd47525afa6a7a8281db209df8f43696d397d471be55f57da892172f48bb4a7da' AS account_number_hash, 'K580ou9+SM6CmedNUHoKDN+HxvMFDaqSw3QCv+ySIslEL/IMkMzK0206' AS resident_number_enc, '77ff47af8f1b096d20eea144f7546c5532256d1c74646c981ccd6ea3a5febea3' AS resident_number_hash UNION ALL
                   SELECT 36 AS n, 'BhzOaYNPRmuIDqBNVWdAaliWXL6u0RpQ/OesPmgCO+eTvDyj3lN0WuvOsQ==' AS account_number_enc, 'a71005c5b39f56c4b43f559b5e73548ff5d97f8c45c8cb55cd056d076f637f8e' AS account_number_hash, 'VG1R84bYTCBSQACE4VmNFfMhq4Q6VX2M7z+3FSgfBocQCp4YNud52tDV' AS resident_number_enc, 'a887e60bd505eda55f5353bd58bc51e33784211944e8439c06a321a54d41ae08' AS resident_number_hash UNION ALL
                   SELECT 37 AS n, 'sRy7l7f6b1R4VDMKAza+j1+tADQH70glqeBjYcuzkkEr1m3DHpNrhqj/pQ==' AS account_number_enc, 'ca43858f357a27ecdb38c62603e867dddf44eb12114874d73d80e8a2e6be39d0' AS account_number_hash, 'gVArosZqUmaCEsfWIM6P5C4TL0QijlD02hQsQv1eYMUerAVGJLtf8Ei9' AS resident_number_enc, '84f37f67402c542075b05c07ce07d038ade081361de9ad6cfad0da15cdd466d0' AS resident_number_hash UNION ALL
                   SELECT 38 AS n, 'cDTe5m6skcTDKnE6MOj8niH9ANqf2H51ai7LEN5ZUI9j45xdLG4c8VnA7A==' AS account_number_enc, '1e6f749b069b447f679fddc78b8dedf3ed309ff1af78aaffb8b429315867afa6' AS account_number_hash, 'BwyWOjyyuBpKjUkdq/9dC6oBaWJe17vwzVpuhEB2OeO7S6vUytNDoucI' AS resident_number_enc, '7289a492817cb8d7643a3ea717386ae5359de312f37bf69556d3204218cc3cc6' AS resident_number_hash UNION ALL
                   SELECT 39 AS n, 'ouci/3VBTV/xblW65g2Z+BV4D1v6w76nbn2k9dfO677FJwppqP6CcicleQ==' AS account_number_enc, '4993298c6522297fd30f85e232416334b3e481ea681fa256a33fe20569aa7b81' AS account_number_hash, 'O05nFoK6D8N5RWPkKRidE/2gdQo/dRe7iFQcGrh+nDC1EGY+FuEmMjtL' AS resident_number_enc, 'd4d7fa79be03a27ce49f7752cd57019aed8d5da1b2fd80cdcd3dacda70779211' AS resident_number_hash UNION ALL
                   SELECT 40 AS n, 'jfMupq3YoXDxWbSB8Lr8q+P9g4oEhC2Jnn4GpQDMgeCcuBBlLz0VYfz9EA==' AS account_number_enc, '89f7179395df8a5d622727798fbfb2e3eee438665958ba2f05de9b2dfca784eb' AS account_number_hash, 'NBovC7eyadeXMuPJqFgh27AcQFWMK9OhE8cQqndwu5Q+5mlgX+8i4Ubd' AS resident_number_enc, 'c76971cbda68c42b1c49c842e11679c932b2cb31bcf9bf2e7c4b521c123a6042' AS resident_number_hash UNION ALL
                   SELECT 41 AS n, 'x5OuokHh39LEmo+S8fFpVku3Ka5jk7Mk10OokbuevKK8USA7Hf1TI0DHlA==' AS account_number_enc, 'c6c6c75900b9bbbbb80d23f231aac60046a404198c14157c01ba2fcb5459eadb' AS account_number_hash, 'RD/FYN+qadV22losx+xqZfAjrit8yZrPTsJYhqAfTewpFrwoHyDPHAt8' AS resident_number_enc, '1f846a86f2a43b4507fb8c212fbd70c0ebaa998b02d745921c93aad1aea7fc89' AS resident_number_hash UNION ALL
                   SELECT 42 AS n, 'yMEsS/OCjLsbtBi53qH/qhD1i0aRr4dVBGWMbRZ3AzWQo/OVvSAhBa+J6g==' AS account_number_enc, '5c1d8ebeb8f9eb656a2d4ebbb3b4584f7265fd6fce23ae018de1bd64d3a1267c' AS account_number_hash, 'v+o5R9kQ8GkgL8S69MK9a8+Q9B7+Ixp8kBuQSvCQTJkwcuUY00L7hbmA' AS resident_number_enc, '1c701d91ed76891cd281a866e417d2d077fa0bc3c8d1e995acfc8f2a9b4cc09e' AS resident_number_hash UNION ALL
                   SELECT 43 AS n, 'BAd/Xu2C8D8ocrNLhxXsTDhAjMOFUXLSMiU+s09ocVMPqze67qV/JCuacA==' AS account_number_enc, '235c74edb400cf7d6b92690a11052209e3454c0807a288d4662d51f1b5779642' AS account_number_hash, 'QijPOfHE910Vj12zIecYFp0YsZvMgbwGvyTIGBQ1ylLrnTvXI8+udtRo' AS resident_number_enc, '89d6bedb42a3085dfb1da2793e0ce00b13daab2b0f7e6cdac9207a63a3773b4c' AS resident_number_hash UNION ALL
                   SELECT 44 AS n, 'VlB4o5m6tLnXllbnF8S8BvyJqMag+Np1gRuKM+NHuLRteeRHbz1dazRX0Q==' AS account_number_enc, '59300fde6d22879c23ca9c4e0f7dc55f7823f3ab7872cb9afa813568920c8754' AS account_number_hash, 'rCsoVIRaV0F3MfsK8oINXVRxEO/H99gsVTU+b8DXf5Dpg2WmPa3jG4uV' AS resident_number_enc, '084e589fa9d47d2e657adeb663abc4d3fed6eb7b709e28724efc8ea4354f029a' AS resident_number_hash UNION ALL
                   SELECT 45 AS n, '5s50IzPcgtRG6Iqgw/Cc9cXpIvuDtjoUv618n5+aiMJiNsbUtjPlhvxzvA==' AS account_number_enc, 'c8f3129a196133cb21dcca67e52eb92b5afe9f2417b5c6d4cca4ee7f12a4d3fe' AS account_number_hash, 'h3VjtZ6meToBPEKPxdYGTluM4Bsu93qdeKAkqXWW9/x1BG4AblGmjNGC' AS resident_number_enc, 'a16fb5d1bd29bc1c3bafca9a3409a0d6319ea92b75950d626fce614566f46554' AS resident_number_hash UNION ALL
                   SELECT 46 AS n, 'U4Cgg+5gSqSp/uetLt4yVppvIslwViigSWoR54n+rp5xDKaPm0T4nG0Nvg==' AS account_number_enc, 'a7ba52f8b169a1f8653470871467636170b941aef2e5b577e88d497e29742de2' AS account_number_hash, 'jdhhLx0GhoK6Qv6E3ebU7YieUAfRJD8PxfdQnmBVslPJDsRFgDr1jFp4' AS resident_number_enc, '14f81df3da5760399f3b3d8d789a2a87bc76e7d0b9b226cb7e86555840e4188a' AS resident_number_hash UNION ALL
                   SELECT 47 AS n, '8F5j1zEL6njbWSYI8eRFKY3CTtuQXMQ4htyP+2FTJe4vvU7NKdBRvRIHYw==' AS account_number_enc, '43167c1bf3adefa9c1e877b6dfee4f82fd9e5c3793045a1610e3a2e705c64366' AS account_number_hash, 'x8XB1NhrLBS4KFwtJLHXzwaGxYmDyLJVl4pte1bn7FlcuxZl7mPcONhH' AS resident_number_enc, 'af93b2e0df389aaa68e3af244a17ffddf8af3bbd8d0d9b73fd9c03e31121a6dc' AS resident_number_hash UNION ALL
                   SELECT 48 AS n, 'qjtGlIqb+bolCad7UwfR2kAbtrYc8Z43l6cJhoFubmfl21MCrQjrEQV2Sg==' AS account_number_enc, 'ca4f4ddc6fb9dc5029d0fdef1844c9efefff97b00108df0e3078eca6a1040423' AS account_number_hash, 'QQDEnFgctToSDpTnytMa9LS9sWg6vkscktfcg2jooMC4kQ/n3M12NOS7' AS resident_number_enc, 'ea3de04075cb4595d0b7b98aa358bf056a7817819d341199769c05923b7cfffd' AS resident_number_hash UNION ALL
                   SELECT 49 AS n, 'uLVfhbdydMF4AlprX0AB7MVP0xs0eBA+HeTfvXo3I9jBQjy2RtEWP10KQw==' AS account_number_enc, '20fd123c925ab964f0e6d13491dc0b8191825f9b7c73ba326d932fdcde5d9a7e' AS account_number_hash, 'YO8Lgwr23E5ApR9xDizZHljfimh6RAv8lD53wa3vMP9Ds2Gv4bgJW5sL' AS resident_number_enc, 'f7b11851c26f1797eb6dd00f3100ebb76a48951a41d7186c5e4d285ec3845131' AS resident_number_hash UNION ALL
                   SELECT 50 AS n, 'IGj2p4pfq7hXMsLXCPY94rMiRV/zzofi1ZRuMbX9MVc9XD4OV5FDrTaRHA==' AS account_number_enc, '8ee82e562dd9774913ae6057699ebbcca2abd8324145c8ce182b0ab2261cea6d' AS account_number_hash, 'mzuz1jrtwWIt6pGHYBNTeLc4GeoBZBFax5YbhmBSqg2ION1XsD6lkFNf' AS resident_number_enc, '9956befbe22f98dd7f9491112a0be0777c2afccdff340a40b5b0213207a2bcd1' AS resident_number_hash UNION ALL
                   SELECT 51 AS n, 'h92DyFE9OUNhtzBIhK5svHAfs5HEl1JrCEr5xbx+FKroIMWIaaxfgJZ5Xg==' AS account_number_enc, '272dbfc434abd3e68ad51778cba28e11d108b8152cf4850811230d897075ad2d' AS account_number_hash, 's1+17Szcoy1p757ovfKM7w6pBViVCMYHnjmKVmCiqw8faNU4uTVShWPb' AS resident_number_enc, 'ec373b48c12215efd97408637c43dd44a5f80ce2f0f2c0af1481b541d9d8a1a5' AS resident_number_hash UNION ALL
                   SELECT 52 AS n, '/VaeKqOpgQ6tVcnfpC1DPIsv0QuVh+6bfR8thr3YrQzDbMVfKyrgdKqbFA==' AS account_number_enc, 'f667ca157dc1d05368a91e20e894514417aa3e03d606ce3fda9014df8c5f6bcc' AS account_number_hash, 'UHJGiea8h/DhdZ+wSBWPY2+/wJy4XW2L+7T6mf8PZPCcn2kb8k6gIiW7' AS resident_number_enc, '5efbb7f95456b3a9444c37d13dbe27b4218bd9989a92434ef9cd58d2c2e25e43' AS resident_number_hash UNION ALL
                   SELECT 53 AS n, '2Ip0Wn3LqkA+Mo3OLv/x6kgOxyf8s3tP4cIhns01KdXwbkEJvUiModAh/Q==' AS account_number_enc, '50a755b45ee2cbee10cef42b2df5f03fbebae76edd06b915e39febc78259ba32' AS account_number_hash, 'DsHQ/pmR/NpCFRB3aC5uxheblNNhhHvmA7SdvmYAXONPuNrvstZC5rnc' AS resident_number_enc, '02e280440f71990ed59dc07cde709245c1db8ae263178f3b511bb90d60c2c521' AS resident_number_hash UNION ALL
                   SELECT 54 AS n, 'EHOmhrXR5srffGkCRwd+dB93z8xGtvnAuQEipDRdMKCNG1K2RvgOStYGfA==' AS account_number_enc, 'e8c5593fec5ce02d0832796a72a76e5a54c2e94a96ea41c40f95eccc88ad121f' AS account_number_hash, 'GfUYjmOg++/YGTo+Bkres9Wpn+BQx3jDvWmVkwSOAigYkrg41oFgBsaw' AS resident_number_enc, 'ef53ea5f2bdbc28d24d2b7b4fb3cc6537b5794592beb1c4d70009a188e938d6b' AS resident_number_hash UNION ALL
                   SELECT 55 AS n, 'Lye8SD+4phJWvgQyU11c6DO4iLhCHgOEuBgGD4oINFC8Fk+8afkQdXsneg==' AS account_number_enc, '999b8555d2ad03bec198c4fa0771fc2285bdc408109112a901b8bcfad2017f12' AS account_number_hash, 'l/z64VHvyVDSVWZ2pNE2Qp5zb9Tmin2W1ozU7jJizsKea3pxIAw+8V5g' AS resident_number_enc, '97c72c3bee56c43c6839567432f674a4fdb36ec6afc8cc480a62d2f15eeedb0d' AS resident_number_hash UNION ALL
                   SELECT 56 AS n, 'Grsh5TeNleOgSpJ1cdUEpgZRPAFP/MGaOMq4pV3CVaGvXeqNvNiQ7M9Ppw==' AS account_number_enc, '724a3f5f0e6471e78fadb918923e2bfaaa0836f1e8858dd2bc1a2dc58e7058db' AS account_number_hash, 'G04TLdjAdQvQkcQzzWah6ZDpPXBqxT8nwQ+pyfnfW8POPSYxAvTfX0ml' AS resident_number_enc, 'd8bd325f0009e556f809f23943c6e3d83700975fe71282e8a1d18d84e7e5b55a' AS resident_number_hash UNION ALL
                   SELECT 57 AS n, 'w3sOhGgbmA7586yP8KCQANFN6dgQxUwBHfcKj8xLKkrsJ5r5WL8FTGMUCw==' AS account_number_enc, 'ffcfca82ae6468afe94ef265c68bc93ede4e4faf28e6f52993238109962a43ff' AS account_number_hash, 'GLW4H+Rus3RKsdYOlhOZNWiYYsezeJuQc+1par2WIN+idrqk8Tg29Gks' AS resident_number_enc, 'a31e5b4e9b6634449c90f4370653269e1856c5b57eecceeca04c5aa8edbb0a0d' AS resident_number_hash UNION ALL
                   SELECT 58 AS n, 'UCfo+CinXicNGBcaBxi7Z2NaxoHm5EGmuL60LEA6CEpWbVMGxGglDt+Khg==' AS account_number_enc, '9198130bb04c6178cad91edde5b77b99bdb59dfcf7d4e7744e8425361f1cdd60' AS account_number_hash, 'DtU90JCiwiJAjGtwoQT7nzE2/bAVYOLRRXJokbGXGyQ6FCz29GZrqPrX' AS resident_number_enc, '42fd09e3106793df9f26cffec646a6c7c32c482ddbc2b791e6e8769c22dbf0b5' AS resident_number_hash UNION ALL
                   SELECT 59 AS n, 'i+j34Ze28k8TCR6kix/h7b+eem4frHOBr9D8vVJYs37yCrETrdLH4h0QXQ==' AS account_number_enc, '547b92b086f28da09908853f5b2a312951263ddec0e1aacb39b1d1f3e4b9e8c3' AS account_number_hash, 'nqS15imKwbZHWHIsmJehOv16O8GqZ0uQd5HDACn1w4NCgqF8CmFmsVTQ' AS resident_number_enc, '85fab3fe70764d75ddce0002a73fb0ef750c75661e022c064eb75861416b28d3' AS resident_number_hash UNION ALL
                   SELECT 60 AS n, '/MnSGWWWYKc6JDfp1dbEJLDHRfKVdstA1ZO8tEKyMCxd/k5/hrFhc/Cxig==' AS account_number_enc, 'd52900b7d65516dbea55090e79eb7aabb00cd0b24f67dcef8d3350c2d52da702' AS account_number_hash, 'OqmL51AllskL9N+AmtuHRMFhvK3W+nu+vvqaVehLDWxfuzWwaERYhPoO' AS resident_number_enc, 'cea80c13a8eca7fad9355c739e648d7cf9f3372cef2372f6393f9a1b56476adf' AS resident_number_hash UNION ALL
                   SELECT 61 AS n, 'yiDRA5yottKlgjKveOq+ntyadBAbGJikbt+Q6sK45OFYIM0djKfZiXGIBQ==' AS account_number_enc, 'b5e70b481cc7247f1471506964c597723eb0bee9bba3edaa0dd6e59c1cc75324' AS account_number_hash, 'DySieEvUCj6CkxQqAHblm8amdPCtrxP7VrVpZdY3tHRUXJsc5LEYddQV' AS resident_number_enc, '76bf5942f91c3d9b4eaea4aef79f5823ac578fb03e6ea7685b896a792709607a' AS resident_number_hash UNION ALL
                   SELECT 62 AS n, '2CxyxrkDUfyhxIGd8hBhkPKgoKU4rhhNXmnqEs0WHPkwM3XZdli0WTcpAQ==' AS account_number_enc, '611d32e50d65bf155be8b0eeb8c6ae2def7d06761325acc5e56295b4080fd296' AS account_number_hash, '3XFXEh/g7xGoGQd4yJV+MO9s6XVexvzArZTplpsu32XyYL36tmkYm1Pq' AS resident_number_enc, '32d7ec38d9fc686c7286d4c95ad11f74a8df1b99c7a878cd07063076d09beb7d' AS resident_number_hash UNION ALL
                   SELECT 63 AS n, 'jCLAaJSddPB7FbhNGOjbrAzW/dwkqEfVOqYUFcIwcNHvJpBFt59qlURABw==' AS account_number_enc, '61e43e90ee0d9d82ac168882d3c93a79b51488bede2f51d3a7bebaa0e9cf33b0' AS account_number_hash, 'dMJfs89+D4U4iy9gRB499ZMC0s3XZjiVN3bgDmpjBG+B79sprjQc5wvr' AS resident_number_enc, 'dc6638ef5497eb85f7a39d66378e802bd3d6d94b0aca00f5ec10e8618802717b' AS resident_number_hash UNION ALL
                   SELECT 64 AS n, 'bW5i/fHRWqJDf4oxPejyqeZ9TlE8r5AjD+X9wVLXZdNdVqp47EoS/SesbA==' AS account_number_enc, '5322d33fbee121f16c7a262c5330218815329dd09788eefdad55ac0e0a3239f8' AS account_number_hash, 'O5nJ3soYImnAWn4MeBlNRINfrfu9b1TsaYIzNbTE5fAgl6P1Q5zccqaL' AS resident_number_enc, 'c6289e90d9683f0bf6fc9be265afdc2143c00cb7c88ac62e3ced52f0b57432e7' AS resident_number_hash UNION ALL
                   SELECT 65 AS n, 'IPjH31SJpltxEgN5JdFiil+R7qT8KRicftHY8bhsu8yY7XkwWESssF2qSw==' AS account_number_enc, '9dc4dd11250e10bf6a0eadccdccf5ba135720435af1aaa031b7a09683ee7aaf2' AS account_number_hash, 'nwAbpBdChoAu2R+ATzK0Cuh0F1pcTZMfX188FKugdG5JPPelxKHi+KuH' AS resident_number_enc, '7ee45da348c38ee021c6783771406c1d8c1e018f1b354396f5e0a7abe1efadb1' AS resident_number_hash UNION ALL
                   SELECT 66 AS n, 'FoWeakV+AlCDmh62jh5jSh/C15wzGg0lMBsPd//kQ4auqfcmbpuCIRNqVA==' AS account_number_enc, '82c5291a9e08a087b37d5b4d36007094b43c11f2eb438e6a71b4703e9a56988c' AS account_number_hash, 'f/RW79sGgrBBDOB0g3VxO4cQpBy2Ab+uep+AU1+n7PxH5EOsy1DYJmTZ' AS resident_number_enc, '72d76f2d63c8202b32ab3874195f9aac11903e027473dcf22c3a18d27acb636f' AS resident_number_hash UNION ALL
                   SELECT 67 AS n, 'jiDGR3Y7tcVb2HQEFEhVwFDBytPSNm1UUSIRVR9AfK5urF3VDyaBj06FIA==' AS account_number_enc, '9dca5c64d4a2748618b365588e57ece0295ec814031148e3fe01dfa1ce920a13' AS account_number_hash, 'Wy0LmgaxcJQ6DXe1Y17vgwKJUEq6aGU1ySVjkHlJjh/JvVEgO4LFG+NL' AS resident_number_enc, '8e5e968ab9385cb483bad83020d1205fccaf64c7757ec69557b730382ab23862' AS resident_number_hash UNION ALL
                   SELECT 68 AS n, 'QSwZXcAXCKD0CB0RK0S8z7HJ7wTaQoTRAQy1OV2kyS/vS1WaSII56n2F6g==' AS account_number_enc, '82e510e413d07f73a3d9c4152c4060624ca0977d14eaa0f449faae4888849ed5' AS account_number_hash, 'VFVhNX9gwY1h/LO3JDXrTUzu6uTcNwwVwl+cl8ssWkYOT7H3CNrhc6wK' AS resident_number_enc, '952a0d691566818958c763bf82905939f0d5a42ff91d822069f32362306c2af4' AS resident_number_hash UNION ALL
                   SELECT 69 AS n, 'gw5yK6ZhQT8/A2XelixF803r33M+LqtwdUrJP/d5jUyKkWnavIoNH9BFqA==' AS account_number_enc, '72d803b512f3668c03d02b768bc32f83524981e9d9bb10258bd95d279b0cfdc7' AS account_number_hash, 'Yto6TjVGGF3Gjn99j6pEcbwiVvgw1VDeYhHNbwzO8qWkpiZlmpygvUUu' AS resident_number_enc, '64cebf4462ad5456f330c8c5b9cb7d99d03a7c161685903ee825dcc3d08ae206' AS resident_number_hash UNION ALL
                   SELECT 70 AS n, 'yGBufpEcjjDCUfrKiEYh6FT4QXhuRxP3Ex2KCI8lpgSJvPudjaXh4RSt+w==' AS account_number_enc, '2c67b151cafbbc211bdf77dc2f0853682995bdef9bf94c2ec3ccd0c91753ec36' AS account_number_hash, 'MlHKyv3FJlz76+CStvj90vBhBbuapIII2+n7wLuFyIy4p7lKInifAsSb' AS resident_number_enc, '11e0e03dfe5adf4a54459581e3e381098487dcfab8043cb338b72ff265d17be7' AS resident_number_hash UNION ALL
                   SELECT 71 AS n, 'Vhg6pBPKStjKILzDDyoFOYwmxtn8bYjeM30paUIiY3EROucqVmfMBvNV1w==' AS account_number_enc, '036206eea8baf3e78f73b21a171c7272145184c13bbe47f47f38a5c553cba2a1' AS account_number_hash, 'JPbTQKeGAE4O8xkgvfrHwlYCVjctjjhw0UYp4lAE68x09OYxcw8vRXRU' AS resident_number_enc, '54021ddaaee105f8d506d58b027d96b1a2720338745b308b1a1e1dc027f74712' AS resident_number_hash UNION ALL
                   SELECT 72 AS n, 'SfrG7/qggjWH0trgssOyJPlR4iy98XzthJTUm+QBe+T7Z+nw5hhiA1SmBQ==' AS account_number_enc, 'faef080e500a149a908ee97092bb28150723711b76cfa8df58e0bc6865b2fc08' AS account_number_hash, 'c9SFiE/i4j4nAL5Ylh4FxeX6TMRETAg82nX7oPEYXkcj39fXCVohZ2tT' AS resident_number_enc, '0a29f14a93f2284698b31fb114a4a7ad1caec9c8749d85b6f546e9a2242902ee' AS resident_number_hash UNION ALL
                   SELECT 73 AS n, 'gaV38rfvN6rBi0xh4iu49cw8xjlGRDBXfVOMOYCIi5s1Jed+tFAy1eFllg==' AS account_number_enc, '2a226c21649f6b08c9889f8476315c1e91ff5d507dd8e21f434250c67dd8c287' AS account_number_hash, 'H5FX+h/+7ocJUz8x6af7x3ZU4pfK07iILYUHeW2QtkLT0whUsMF5/7hE' AS resident_number_enc, 'df6fe528dfcdb9a535fb3039c8a23b1948d122cef21aedc60cdcfda93e356ab2' AS resident_number_hash UNION ALL
                   SELECT 74 AS n, 'bM3XW+wCv6GzV0RQuz5plxMTBiN/VgrvgUTh11fkIH9vWyQG4wlvaTY0pQ==' AS account_number_enc, 'f62af8b1f0f6a341a3e195d93320f3cb55b6d416ae7123ec1a3a3b5bcb4fdd3d' AS account_number_hash, '5D1WD38bCfAeHI7DZducakQYQ5aRQXdGBhoWbQ4iqnVa00TQCALq9hRr' AS resident_number_enc, 'ee17d8247bad7c1f28af1f37f95995a23a5381eab9312b3cc55acca2a8dd2131' AS resident_number_hash UNION ALL
                   SELECT 75 AS n, '8ptT1b/a1JnSey88YjOITCBGZ1cPhivIjFEJwMATPJsEnQqtrN5oMViN9A==' AS account_number_enc, 'caa0b66fc82f5fd24ed46984df187eea1c6a71b4e0952a46630aa1e2571c3e88' AS account_number_hash, 'BRhw15TNxEJWqcKh6RmMUwqJZgQFRy6zUVk5eoLmaErq0Pcji+Lk34v5' AS resident_number_enc, 'a8fc241fb576d09c193301b8897c3f7401a1604272eadb7f3975d25d77c83491' AS resident_number_hash UNION ALL
                   SELECT 76 AS n, 'WsS0hk/8ocuUIGyxWABHIuitJEvOSu2hJXM3ziiQHKI0ZfDzj6fdOJ/dvw==' AS account_number_enc, '4c98fe2a0c97fd26a98420edb8b8b14185eca660a3165a4e52952b1470896298' AS account_number_hash, 'OdlKA8A2zmWkbAttZSdpP6FRJ65yp2aCXmqW+onfIHRwLXE7QQCn1fna' AS resident_number_enc, '97a32e7cc036d11b7c6d5a0c31c7ff7ac41b83450496bf061f749cf2797d918d' AS resident_number_hash UNION ALL
                   SELECT 77 AS n, 'mW9r4s3VBJUTHZiqQk+iOApJu9ObhbxN8cK6QlCSUbltoF1oH5gls1DfKQ==' AS account_number_enc, '515962dcf4d8a7ac4b2f76bddc79fe945e6c54d185b14802b61291f3ee0081c2' AS account_number_hash, 'eydZznc/gxFuyC0N8sCU8Pf6MkJ5k11678xR4BnnNPkOf5euBbMZqvQ/' AS resident_number_enc, 'e715ecc11ae357b5da18806b3e77e52c9ed33cb624dd2c421066c13b936e8e28' AS resident_number_hash UNION ALL
                   SELECT 78 AS n, '9lwXcX+5v09pjqh+xW9vx+oX8Hna8T4orax/B84SmmKLWPqUDoUVWEi9JQ==' AS account_number_enc, '0dd7ba170657b2b8614ba6435483cbc868d03ae59a119c6bedef261a2674a7d3' AS account_number_hash, 'wOOqvhHeEtr0rAaKUwnL/IAc82zyYDYWH1a5zh7ylKQz1zKrvmRcT/5M' AS resident_number_enc, 'aa0f168da69ad56ece1ec50c09dccc539c769fcc472c834fefd64e338fe0c2e0' AS resident_number_hash UNION ALL
                   SELECT 79 AS n, '5HSvVjkpUMmnCvlEnT1nH0/QyKHrJjv6s78kSNJMguGgYiIjW1RE1zTSEA==' AS account_number_enc, '6c79899470324ed59859044a492de76a87670854627a7d62f7a961beb3ae9dfa' AS account_number_hash, 'F9uoaW00urj5WG1RbfygUG7/hucw/6I+eir4StL0/XsCqbq3KHj/5rTv' AS resident_number_enc, '4811c13703076c37a7896e48305a9d75a7c217e9826371eb528885da378a849e' AS resident_number_hash UNION ALL
                   SELECT 80 AS n, 'Innqmuc2hhbUnxLSTrMz07XVnCx5rISXH6aIvzKQ/ji4jcrFP900tiI/ZA==' AS account_number_enc, '5577ac489efd7438e9464bbfa85f12dacf1219d6e12eee12394438690b782f3f' AS account_number_hash, '1QHjR334OdtMPoNHY1nS8geakR5fybzuoptiL4KgUiv47OdD7XFrtIZ0' AS resident_number_enc, '2cd22e39c8d7ef46b60bbd11b59fa74d6196b25b192d8b529480d64f79c02738' AS resident_number_hash UNION ALL
                   SELECT 81 AS n, 'Fl6S7+wRGsIQ3l4ugGjD/qaIph7YWFmhHQ274L7BN0h+HTW4RS29gphxnw==' AS account_number_enc, 'fdf3006f3e6815f5b8e69f34bcc2b4a3b4b7edc83a2e7cdf63e5436e91e0639b' AS account_number_hash, 'X5//HsjQAEsp5KbdaPyY/AXyQ69USO1kiDP518D2lD79e87dqUGba934' AS resident_number_enc, '747f85bfba9a5ef5af5ba0396ff45990e37944b0e4b88c061cd1a44294a9c6ba' AS resident_number_hash UNION ALL
                   SELECT 82 AS n, 'QjdePOPZ6HohAHIzXbRMVco7+Igm0REbkA/VzQa/vqq74uUmQ4jCL9migA==' AS account_number_enc, '998c154cb6ea638e9955d376a33de9568ca77907ffbbb7c6084b3a9c4d2b98d0' AS account_number_hash, 'R8azajqMHhfsHQNU7GkAuK8lj4fkWFZ22YqCrmlPauQEA+64uvf6LQ/6' AS resident_number_enc, '087dec28bd2a9036147b6fcb10014ab347a90454d83de169f39248250c81913a' AS resident_number_hash UNION ALL
                   SELECT 83 AS n, '8+8Xu+5HETC+X95qzaUAb8+IMh7546hWRxz2B4MrLxNgiRXrKitokOer1w==' AS account_number_enc, '0b0ccd952ee67021c67b3b0a6be18839ee60818d56464f6ed06aa375ba25d16a' AS account_number_hash, '9sugcmMkTFwDRilhWIHEgQSF5HmAzinwRwsY/OzpYO4g0HIcYRCdoTk1' AS resident_number_enc, '99e1b0711493ff94b47be244500adddd4b6df7e20191408fbbbb6619954f6d36' AS resident_number_hash UNION ALL
                   SELECT 84 AS n, 'Tp3Fa9m1aufIiUzwrmQ/48AtkwEsQ2mNyxlvH9/lvIP+9qvRru42Cq8g5A==' AS account_number_enc, 'b168353b0d5fdb7285f6496d713a158986658802ca293e9bf7291153713347c3' AS account_number_hash, 'y+lXVmtxGX/XkMsWM/nKMWkvdYGs8bbZmVa61rAzpoHiJCfQd3NloP4h' AS resident_number_enc, '3834b08fc49f4095193e3b681b60541b841408aff7ad6b3e7769740b245c3a61' AS resident_number_hash UNION ALL
                   SELECT 85 AS n, 'yZUpY9HpI+MAYziI9wp/AxXnnKwIXOC7Fj1GfGcCYk/TMR3npTcSrJkYIg==' AS account_number_enc, 'b1f8ec71a23c69bfb266ec4648dd08a80f16ad313a4b9717ebe664a4a5aa55dc' AS account_number_hash, 'RO+pIDKhu1NbIJMveXHDayRTrTiXrVUo1T4m0BRSaXr09qMoXb/otKu5' AS resident_number_enc, '697d5cf61fcf7de1edfbcf129f0aec44ef59cacd5c2c290e6ed25f7869695cdc' AS resident_number_hash UNION ALL
                   SELECT 86 AS n, 'rKuQJJxpz1pA1e2gN1OtV/3AYXSQeXSAC5akp5544Yy+4xX+3A+y0n3oKQ==' AS account_number_enc, 'c687ed29cd5f8821ddcc7547c142549cb860cc2b4d00dc0506bd31ecb54a15c9' AS account_number_hash, 'cpK26AHh6/KUfAAgr08AaBT2BXqS2DHnEOZL/FUV5I2Jj3Fc+QqdNL6F' AS resident_number_enc, '9f2953ab302def380bb04009f5b752edefc188eb29f8d6407a999ce0cebcc0ff' AS resident_number_hash UNION ALL
                   SELECT 87 AS n, 'I2DhuNllqmhCn6AEvvELR83rFNKOpQoSluTItsW2y+YlnD+ptQUH76moHQ==' AS account_number_enc, 'd4b961a8dff5078669b1674e91767cb3c4ceee0baae70be94d700ab598139427' AS account_number_hash, 'RnAu2DNu0aclaVzKEKi75ufnKK0Y3+xA8aR+n8Sb+lNyIaH3nzrYI9ml' AS resident_number_enc, 'e17f0c2e9fd49d041deff66c71d5519c7f8df58716b83e8aa4acf87279856d7a' AS resident_number_hash UNION ALL
                   SELECT 88 AS n, 'WrChzolkEIB572eG44SJ3/X9lNPIB+6buBnYzGkVJod3/IxmF+MU1sXFQA==' AS account_number_enc, '9353de5eeb8ec658feb9e6b1443bd94999d3f54e4a2c625cd1540f8ae219bbb0' AS account_number_hash, 'GzHuYmt0pY1rzOgNH+oxpIY39SD5n8Fl6X6Uu5KPp47GM3+WpFLLZGxV' AS resident_number_enc, '733cf446469ac0b07ba11749bcf5bd2d1f2cac3dc5eb9787028fff557927ee3a' AS resident_number_hash UNION ALL
                   SELECT 89 AS n, 'vvbiz5SnDhwMO06IAp+6ffp9jg8g3902PkAz7Du82qNbGwSYtwShDyhw2Q==' AS account_number_enc, 'e27338972f6582cc0e64270f2ac9d6da6af32f5e071b65693a35e5cc20f8d4fe' AS account_number_hash, 'A6ABNSryhOUxOQeY+DEEHt5xDxm8DNRxTdhDnbTlok/b8wkUKSJLIumE' AS resident_number_enc, '78335609a78ea4dba569a7c2180b7778c4e9e66fb5d2cc45c2dbcdc9c70cae2f' AS resident_number_hash UNION ALL
                   SELECT 90 AS n, 'YXY9IILfeQlcAQbChmTfxLRxXlB8UukVoP0Nvq1MLQWau2GAswmly9IMcw==' AS account_number_enc, 'fe639336fc663f4c5b54207b0f8c01764124522369b7249bb4d44279580432d6' AS account_number_hash, 'HwM5vQMEsB+y4g5rVW3pDvPpkMBXLR/bOJwYLTny/O2Be0A+hn4j7D/Y' AS resident_number_enc, 'b0ee4978aed9f5b64d83187638c3f9de906212c3db225430d83645c82a84dd24' AS resident_number_hash UNION ALL
                   SELECT 91 AS n, 'JJY0IxB6z0rmvj8mL/nQr5y7+9qOBZQfq0HmBSC33EeIuAW/f6QO/h2r9A==' AS account_number_enc, 'aea92eb8db98692801e21cd7b65b1e7733a97e47578596d4ec8e0e6d05eaa12b' AS account_number_hash, 'uWb2zHw797rVh0HV8G4hmKmNIcw2py+wQmA6HOl8/dKd1bU9Imiie4/k' AS resident_number_enc, 'df5b5d0a0aa613e83064a60795e43d4e6284cde074241ea07f129c3548297f45' AS resident_number_hash UNION ALL
                   SELECT 92 AS n, 'CXcp0cjzxN97hY8MMeDP/RL7DdZzvr/QdLGFmOHEdaSK8QY5IHA5Mu7pTg==' AS account_number_enc, 'b3dc1e759d89ee09035e34bc0d5534900091875b763397190fa23aa9bf3de1af' AS account_number_hash, 'vlC7m4lXLt6BYThMOTifU74bzy06VokA769DOHPeEuXrAOCjLp6e9nuh' AS resident_number_enc, 'd04ef72f90d05d8d0abc88a3029190d82014547042e63df8ca40d91d7a5027e2' AS resident_number_hash UNION ALL
                   SELECT 93 AS n, 'CvXL+FLqdeGMUZlhenfKzliRk+1oQJJ7fElCjZVqaf4k+P9dBLtrI3Is4w==' AS account_number_enc, 'bb6a4e4fb4dbf2f1733df1c37cb8af42728a42ae2e0a104f1c472b9b4871bc44' AS account_number_hash, 'SYaS5Ds0kKgDJWV39wkJ7Ti5xAVzgZzHjADWPvMq9wNEmV9vKWn9B+6A' AS resident_number_enc, 'c5cfea6c98b82c7a09d6b59054683f449adff81a8e9b73ae96e069cbf2a80b8a' AS resident_number_hash UNION ALL
                   SELECT 94 AS n, 'rku0j+wkVx8x1ZJ5b+WYvNfEiDQMfzv0KoSeoVfQosrhOAjTzEE/z6B/NA==' AS account_number_enc, 'e633e064c4d1305848ab399821eaf2aa2d8863df72ce5e0a547ffcf2c570a85d' AS account_number_hash, 'FgiHhEFenOqz0dyet1eCr0aVp/gp/n4X9QyFLOW2o+oONysJjFM4kih8' AS resident_number_enc, '12ee55c9c37e2e1e1d7c3f56762b0eafe7b26711af6de129adc22a093ccae4ee' AS resident_number_hash UNION ALL
                   SELECT 95 AS n, 'rni2m6C0jG4Q+wo6MsQ/HxR5U5bhCgL5ycj3jiBn2NTnNNZCeisudzCJiA==' AS account_number_enc, '0722c7317f679f8ec5f38324d2d82611e6541ce45bd51b68cd884e93db9f759b' AS account_number_hash, 'fFkHVls0qdSzdANIbVSPO4slP9J5ol4Ga9o4B0jDKEhzl+5VC1LQgHk/' AS resident_number_enc, 'f7bc9c516af5ca424131caf66abc97dfafd6a2735ed92ed0c54dca73bf796552' AS resident_number_hash UNION ALL
                   SELECT 96 AS n, 'j3b4j21gPhMgeV4BOWmgXnt4O7ektajQkWqCo3a8sTOlZIOyFeu0sTZK6w==' AS account_number_enc, '7ee3642d42bed2ae02c3d234c44e5a03a0a05c3a26799ad22b0b92b9a749c90d' AS account_number_hash, '8RZJXv+AZD4ghRuGrlKZBpS2KHS7ZOP3oss4OSWXMdXJiM2sjYu5bCmL' AS resident_number_enc, 'cea43195788f070dddff23a46a44a9e5f96592f66173644e5d5ccf07ab87de57' AS resident_number_hash UNION ALL
                   SELECT 97 AS n, 'KG2gg/3pGzw97o0qFzdsmIPMmSzzujz6POJbmOO5egXClumX/zMxauwjXw==' AS account_number_enc, '8eed5a95d44a373252ae5d7879e4e3c9413232fbb249ae03b66be38de7aace67' AS account_number_hash, 'cAo03/LqmRIg4Fl649j4UhslzHsXZXJMNYYnhKitKLX6xWPKsfq1n09N' AS resident_number_enc, '464c1bfc886af220aeefda5c4f965788b201bd1b2853017c4147d64996884d21' AS resident_number_hash UNION ALL
                   SELECT 98 AS n, 'dDhwvQT5uMrFWtKrf9JasEeIPbv19hhF1FNQX3fuimiCvpo59DKlRoaTdg==' AS account_number_enc, 'ad59ce836584c844a8e84d84f0e155287252cd82ed07007f9a3ce38e4fe0a10b' AS account_number_hash, 'HX/mO3FjqqL+5j/M3vobc1hU8niN3lDPjxPeLdcsuPcrVzZxseFGt6pd' AS resident_number_enc, '2fb8a5d69cdf12c42a902df9538c0186d556f40f84146a900b6b269e4203c3f9' AS resident_number_hash UNION ALL
                   SELECT 99 AS n, '9Marj3FIzB9b9hQoUxKxIlUs9w5h3oNDZRPLUhz1j3cCJoMAHpTkVhnvvw==' AS account_number_enc, '15962d9fe803da3c79a7c329165af02abf1d822a76f16a3573ff91242e05ff35' AS account_number_hash, 'TIjFAE/NPlfPt1KjCUXbtZ5OJ6fWQ5ss0CXSOYH6h6VYN/Dme0vae+o0' AS resident_number_enc, 'aaf3cfc972820094602cc4aabbe25e08d1f8436ebf9ac28bb0e1b431e597828e' AS resident_number_hash UNION ALL
                   SELECT 100 AS n, '7UU8j5viMi6CLfXQwHi+dgc1YRTCfIWJ32M4Idc1ns3I8dSMP78RzKT6Rg==' AS account_number_enc, 'bcb2b75eb2dfc0e56e1c9786e162bb1285f3cde0db3f527766aa9bcfd38e4eb0' AS account_number_hash, 'tJ4sGAiIyf9/hQSX81JnnrRho9WwySzYqf+82T9a3yZE9t6uzMXBb62R' AS resident_number_enc, 'a2f078230848741c8e24b9e73c871af40938230534bbd1b83b10eca9a7998390' AS resident_number_hash
               )
SELECT
    CONCAT(DATE_FORMAT(es.hire_date, '%y%m%d'), LPAD(es.day_seq, 4, '0')) AS employee_num,
    CASE es.n
        WHEN 1 THEN '강동윤'
        WHEN 2 THEN '김수현'
        WHEN 3 THEN '박보검'
        WHEN 4 THEN '이준호'
        WHEN 5 THEN '정해인'
        WHEN 6 THEN '조인성'
        WHEN 7 THEN '유연석'
        WHEN 8 THEN '남주혁'
        WHEN 9 THEN '차은우'
        WHEN 10 THEN '한지민'
        WHEN 11 THEN '손예진'
        WHEN 12 THEN '김태리'
        WHEN 13 THEN '전지현'
        WHEN 14 THEN '수지은'
        WHEN 15 THEN '윤아린'
        WHEN 16 THEN '문가영'
        WHEN 17 THEN '신세경'
        WHEN 18 THEN '배수빈'
        WHEN 19 THEN '고민시'
        WHEN 20 THEN '오연서'
        WHEN 21 THEN '강하늘'
        WHEN 22 THEN '김우빈'
        WHEN 23 THEN '박서준'
        WHEN 24 THEN '이도현'
        WHEN 25 THEN '최민호'
        WHEN 26 THEN '정우성'
        WHEN 27 THEN '조정석'
        WHEN 28 THEN '유승호'
        WHEN 29 THEN '남윤호'
        WHEN 30 THEN '차태현'
        WHEN 31 THEN '한효주'
        WHEN 32 THEN '손나은'
        WHEN 33 THEN '김지원'
        WHEN 34 THEN '전여빈'
        WHEN 35 THEN '수현아'
        WHEN 36 THEN '윤세아'
        WHEN 37 THEN '문채원'
        WHEN 38 THEN '신예은'
        WHEN 39 THEN '배두나'
        WHEN 40 THEN '고아성'
        WHEN 41 THEN '강민혁'
        WHEN 42 THEN '김동욱'
        WHEN 43 THEN '박형식'
        WHEN 44 THEN '이재훈'
        WHEN 45 THEN '최우식'
        WHEN 46 THEN '정경호'
        WHEN 47 THEN '조진웅'
        WHEN 48 THEN '유아인'
        WHEN 49 THEN '남다름'
        WHEN 50 THEN '차서원'
        WHEN 51 THEN '한소희'
        WHEN 52 THEN '손담비'
        WHEN 53 THEN '김고은'
        WHEN 54 THEN '전미도'
        WHEN 55 THEN '수지민'
        WHEN 56 THEN '윤보라'
        WHEN 57 THEN '문소리'
        WHEN 58 THEN '신민아'
        WHEN 59 THEN '배윤경'
        WHEN 60 THEN '고현정'
        WHEN 61 THEN '강태오'
        WHEN 62 THEN '김선호'
        WHEN 63 THEN '박정민'
        WHEN 64 THEN '이상이'
        WHEN 65 THEN '최현욱'
        WHEN 66 THEN '정가람'
        WHEN 67 THEN '조복래'
        WHEN 68 THEN '유지태'
        WHEN 69 THEN '남윤수'
        WHEN 70 THEN '차승원'
        WHEN 71 THEN '한예리'
        WHEN 72 THEN '손은서'
        WHEN 73 THEN '김세정'
        WHEN 74 THEN '전소민'
        WHEN 75 THEN '수아현'
        WHEN 76 THEN '윤주희'
        WHEN 77 THEN '문근영'
        WHEN 78 THEN '신현빈'
        WHEN 79 THEN '배혜선'
        WHEN 80 THEN '고보결'
        WHEN 81 THEN '강기영'
        WHEN 82 THEN '김남길'
        WHEN 83 THEN '박성훈'
        WHEN 84 THEN '이준혁'
        WHEN 85 THEN '최진혁'
        WHEN 86 THEN '정건주'
        WHEN 87 THEN '조병규'
        WHEN 88 THEN '유태민'
        WHEN 89 THEN '남궁민'
        WHEN 90 THEN '차학연'
        WHEN 91 THEN '한예슬'
        WHEN 92 THEN '손예림'
        WHEN 93 THEN '김민하'
        WHEN 94 THEN '전종서'
        WHEN 95 THEN '수현빈'
        WHEN 96 THEN '윤세리'
        WHEN 97 THEN '문채린'
        WHEN 98 THEN '신유나'
        WHEN 99 THEN '배지은'
        ELSE '고은별'
        END AS employee_name,
    '$2y$10$Xo6/MfFZfMOq0N5d76a/d.gcEVFHSVT4nBbRdZmpZ7GKwHgHKN7kK' AS employee_password,
    CONCAT('0107', LPAD(es.n, 7, '0')) AS phone,
    CONCAT('E', LPAD(2000 + es.n, 4, '0')) AS ext,
    CONCAT('employee', LPAD(es.n, 3, '0'), '@seed.rhight.local') AS email,
    CONCAT('서울시 강남구 테스트로 ', es.n) AS address,
    DATE_ADD(DATE('1985-01-01'), INTERVAL es.n * 120 DAY) AS birth_date,
    ELT(MOD(es.n - 1, 5) + 1, '국민은행', '신한은행', '하나은행', '우리은행', '기업은행') AS bank_name,
    ss.account_number_enc AS account_number_enc,
    ss.account_number_hash AS account_number_hash,
    ss.resident_number_enc AS resident_number_enc,
    ss.resident_number_hash AS resident_number_hash,
    FALSE AS initial_state,
    CASE
        WHEN es.n BETWEEN 97 AND 98 THEN 'LEAVE'
        WHEN es.n >= 99 THEN 'RESIGN'
        ELSE 'WORK'
        END AS employ_state,
    es.hire_date,
    dp.hr_file_id AS profile_id
FROM employee_seed es
         JOIN (
    SELECT hf.hr_file_id
    FROM hr_file hf
    WHERE hf.file_key = 'hr/profile/basicprofile.webp'
    ORDER BY hf.hr_file_id
    LIMIT 1
) dp
         JOIN sensitive_seed ss ON ss.n = es.n
         LEFT JOIN employee e
                   ON e.employee_num = CONCAT(DATE_FORMAT(es.hire_date, '%y%m%d'), LPAD(es.day_seq, 4, '0'))
WHERE e.employee_id IS NULL;

-- ---------------------------------------------------------------------------
-- Employee HR info (bulk insert)
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
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1
    FROM seq
    WHERE n < 100
),
               employee_seed AS (
                   SELECT
                       n,
                       DATE_ADD(DATE('2025-02-12'), INTERVAL FLOOR((n - 1) / 10) DAY) AS hire_date,
                       MOD(n - 1, 10) + 1 AS day_seq
                   FROM seq
               )
SELECT
    e.employee_id,
    CASE
        WHEN es.n = 1 THEN 9001
        WHEN es.n = 2 THEN 9101
        WHEN es.n = 3 THEN 9102
        WHEN es.n = 4 THEN 9103
        WHEN es.n = 5 THEN 9201
        WHEN es.n = 6 THEN 9202
        WHEN es.n = 7 THEN 9203
        WHEN es.n = 8 THEN 9204
        WHEN es.n = 9 THEN 9205
        WHEN es.n = 10 THEN 9301
        WHEN es.n = 11 THEN 9302
        WHEN es.n = 12 THEN 9303
        WHEN es.n = 13 THEN 9304
        WHEN es.n = 14 THEN 9305
        WHEN es.n = 15 THEN 9306
        WHEN es.n = 16 THEN 9307
        WHEN es.n = 17 THEN 9308
        WHEN es.n = 18 THEN 9309
        ELSE ELT(MOD(es.n - 19, 9) + 1, 9301, 9302, 9303, 9304, 9305, 9306, 9307, 9308, 9309)
        END AS org_id,
    es.hire_date AS effective_from,
    p.position_id,
    r.rank_id,
    j.job_id,
    CASE
        WHEN es.n <= 85 THEN 'REGULAR'
        WHEN es.n <= 95 THEN 'CONTRACT'
        ELSE 'NON_REGULAR'
        END AS employ_type,
    CASE WHEN es.n <= 40 THEN 'EXPERIENCED' ELSE 'NEW' END AS recruit_type,
    a.area_id
FROM employee_seed es
         JOIN employee e
              ON e.employee_num = CONCAT(DATE_FORMAT(es.hire_date, '%y%m%d'), LPAD(es.day_seq, 4, '0'))
         LEFT JOIN employee_hr_info ehr ON ehr.employee_id = e.employee_id
         JOIN hr_position p
              ON p.position_name = CASE
                                       WHEN es.n = 1 THEN 'CEO'
                                       WHEN es.n BETWEEN 2 AND 4 THEN '본부장'
                                       WHEN es.n BETWEEN 5 AND 6 THEN '센터장'
                                       WHEN es.n BETWEEN 7 AND 8 THEN '실장'
                                       WHEN es.n = 9 THEN '센터장'
                                       WHEN es.n BETWEEN 10 AND 18 THEN '팀장'
                                       WHEN MOD(es.n, 17) = 0 THEN '파트장'
                                       ELSE '팀원'
                  END
         JOIN hr_rank r
              ON r.rank_name = CASE
                                   WHEN es.n = 1 THEN '사장'
                                   WHEN es.n = 2 THEN '부사장'
                                   WHEN es.n = 3 THEN '전무'
                                   WHEN es.n = 4 THEN '상무'
                                   WHEN es.n BETWEEN 5 AND 6 THEN '이사'
                                   WHEN es.n BETWEEN 7 AND 9 THEN '부장'
                                   WHEN es.n BETWEEN 10 AND 18 THEN '차장'
                                   WHEN MOD(es.n, 13) = 0 THEN '과장'
                                   WHEN MOD(es.n, 7) = 0 THEN '대리'
                                   WHEN MOD(es.n, 5) = 0 THEN '주임'
                                   ELSE '사원'
                  END
         JOIN job j
              ON j.job_name = CASE
                                  WHEN es.n IN (1, 2) THEN '경영지원'
                                  WHEN es.n IN (5, 10, 19, 28, 37, 46, 55, 64, 73, 82, 91) THEN '인사'
                                  WHEN es.n IN (6, 11, 20, 29, 38, 47, 56, 65, 74, 83, 92) THEN '재무'
                                  WHEN es.n IN (7, 13, 22, 31, 40, 49, 58, 67, 76, 85, 94) THEN '백엔드 개발'
                                  WHEN es.n IN (8, 14, 23, 32, 41, 50, 59, 68, 77, 86, 95) THEN '프론트엔드 개발'
                                  WHEN es.n IN (15, 24, 33, 42, 51, 60, 69, 78, 87, 96) THEN '모바일 개발'
                                  WHEN es.n IN (16, 25, 34, 43, 52, 61, 70, 79, 88, 97) THEN 'QA'
                                  ELSE '영업'
                  END
         JOIN working_area a
              ON a.area_name = CASE
                                   WHEN es.n <= 40 THEN '서울 본사'
                                   WHEN es.n <= 70 THEN '판교 캠퍼스'
                                   WHEN es.n <= 90 THEN '부산 지사'
                                   ELSE '원격'
                  END
WHERE ehr.employee_hr_id IS NULL;

-- ---------------------------------------------------------------------------
-- Role mapping
-- 모든 사원은 EVALUATEE 기본 부여
-- ---------------------------------------------------------------------------
INSERT INTO employee_role (employee_id, role_id)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1
    FROM seq
    WHERE n < 100
),
               employee_seed AS (
                   SELECT
                       n,
                       DATE_ADD(DATE('2025-02-12'), INTERVAL FLOOR((n - 1) / 10) DAY) AS hire_date,
                       MOD(n - 1, 10) + 1 AS day_seq
                   FROM seq
               )
SELECT
    e.employee_id,
    r.role_id
FROM employee_seed es
         JOIN employee e
              ON e.employee_num = CONCAT(DATE_FORMAT(es.hire_date, '%y%m%d'), LPAD(es.day_seq, 4, '0'))
         JOIN role r ON r.role_code = 'EVALUATEE'
         LEFT JOIN employee_role er
                   ON er.employee_id = e.employee_id
                       AND er.role_id = r.role_id
WHERE er.employee_id IS NULL;

INSERT INTO employee_role (employee_id, role_id)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1
    FROM seq
    WHERE n < 100
),
               employee_seed AS (
                   SELECT
                       n,
                       DATE_ADD(DATE('2025-02-12'), INTERVAL FLOOR((n - 1) / 10) DAY) AS hire_date,
                       MOD(n - 1, 10) + 1 AS day_seq
                   FROM seq
               )
SELECT
    e.employee_id,
    r.role_id
FROM employee_seed es
         JOIN employee e
              ON e.employee_num = CONCAT(DATE_FORMAT(es.hire_date, '%y%m%d'), LPAD(es.day_seq, 4, '0'))
         JOIN role r ON r.role_code = 'EVALUATOR'
         LEFT JOIN employee_role er
                   ON er.employee_id = e.employee_id
                       AND er.role_id = r.role_id
WHERE es.n <= 18
  AND er.employee_id IS NULL;

INSERT INTO employee_role (employee_id, role_id)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1
    FROM seq
    WHERE n < 3
),
               employee_seed AS (
                   SELECT
                       n,
                       DATE_ADD(DATE('2025-02-12'), INTERVAL FLOOR((n - 1) / 10) DAY) AS hire_date,
                       MOD(n - 1, 10) + 1 AS day_seq,
                       CASE
                           WHEN n = 1 THEN 'HR_ADMIN_MASTER'
                           WHEN n = 2 THEN 'HR_ADMIN_PAYROLL'
                           ELSE 'HR_ADMIN_BASIC'
                           END AS role_code
                   FROM seq
               )
SELECT
    e.employee_id,
    r.role_id
FROM employee_seed es
         JOIN employee e
              ON e.employee_num = CONCAT(DATE_FORMAT(es.hire_date, '%y%m%d'), LPAD(es.day_seq, 4, '0'))
         JOIN role r ON r.role_code = es.role_code
         LEFT JOIN employee_role er
                   ON er.employee_id = e.employee_id
                       AND er.role_id = r.role_id
WHERE er.employee_id IS NULL;

-- ---------------------------------------------------------------------------
-- Attendance seed
-- ---------------------------------------------------------------------------
INSERT INTO attendance_policy (
    employee_id,
    policy_name,
    std_start_time,
    std_end_time,
    core_time_start,
    core_time_end,
    break_time_start,
    break_time_end,
    created_at,
    updated_at
)
SELECT
    e.employee_id,
    CASE
        WHEN p.position_name IN ('CEO', '본부장', '센터장', '실장') THEN '임원 유연근무제'
        WHEN p.position_name IN ('팀장', '파트장') THEN '리더 표준근무제'
        ELSE '일반 선택근무제'
        END AS policy_name,
    CASE
        WHEN p.position_name IN ('CEO', '본부장', '센터장', '실장') THEN '08:00:00'
        WHEN p.position_name IN ('팀장', '파트장') THEN '08:30:00'
        ELSE '09:00:00'
        END AS std_start_time,
    CASE
        WHEN p.position_name IN ('CEO', '본부장', '센터장', '실장') THEN '17:00:00'
        WHEN p.position_name IN ('팀장', '파트장') THEN '17:30:00'
        ELSE '18:00:00'
        END AS std_end_time,
    '10:00:00' AS core_time_start,
    '16:00:00' AS core_time_end,
    '12:00:00' AS break_time_start,
    '13:00:00' AS break_time_end,
    '2026-03-01 09:00:00' AS created_at,
    '2026-03-01 09:00:00' AS updated_at
FROM employee e
         JOIN employee_hr_info ehr ON ehr.employee_id = e.employee_id
         JOIN hr_position p ON p.position_id = ehr.position_id
         LEFT JOIN attendance_policy ap ON ap.employee_id = e.employee_id
WHERE e.email LIKE '%@seed.rhight.local'
  AND ap.policy_id IS NULL;

INSERT INTO leave_balance (
    employee_id,
    base_year,
    total_annual_leave,
    used_annual_leave
)
SELECT
    e.employee_id,
    seed.base_year,
    seed.total_annual_leave,
    seed.used_annual_leave
FROM employee e
         JOIN (
    SELECT 2025 AS base_year, 15.0 AS total_annual_leave, 6.0 AS used_annual_leave
    UNION ALL
    SELECT 2026, 15.0, 2.0
) seed
         LEFT JOIN leave_balance lb
                   ON lb.employee_id = e.employee_id
                       AND lb.base_year = seed.base_year
WHERE e.email LIKE '%@seed.rhight.local'
  AND lb.vacation_id IS NULL;

INSERT INTO attendance_record (
    employee_id,
    work_date,
    check_in_time,
    check_out_time,
    status,
    tardy_reason,
    modify_reason,
    overtime_hours,
    night_work_hours,
    holiday_work_hours,
    is_unpaid_leave,
    is_closed
)
WITH RECURSIVE work_days AS (
    SELECT DATE('2026-03-02') AS work_date
UNION ALL
SELECT DATE_ADD(work_date, INTERVAL 1 DAY)
FROM work_days
WHERE work_date < DATE('2026-03-06')
    ),
    seed_employees AS (
SELECT
    e.employee_id,
    ROW_NUMBER() OVER (ORDER BY e.employee_id) AS rn
FROM employee e
WHERE e.email LIKE '%@seed.rhight.local'
  AND e.employ_state <> 'RESIGN'
    )
SELECT
    se.employee_id,
    wd.work_date,
    CASE
        WHEN MOD(se.rn + DAY(wd.work_date), 19) = 0 THEN NULL
        WHEN MOD(se.rn + DAY(wd.work_date), 11) = 0 THEN '09:31:00'
        WHEN MOD(se.rn + DAY(wd.work_date), 7) = 0 THEN '08:47:00'
        ELSE '09:02:00'
        END AS check_in_time,
    CASE
        WHEN MOD(se.rn + DAY(wd.work_date), 19) = 0 THEN NULL
        WHEN MOD(se.rn + DAY(wd.work_date), 13) = 0 THEN '16:54:00'
        WHEN MOD(se.rn + DAY(wd.work_date), 5) = 0 THEN '19:12:00'
        ELSE '18:06:00'
        END AS check_out_time,
    CASE
        WHEN MOD(se.rn + DAY(wd.work_date), 19) = 0 THEN 'VACATION'
        WHEN MOD(se.rn + DAY(wd.work_date), 17) = 0 THEN 'BUSINESS_TRIP'
        WHEN MOD(se.rn + DAY(wd.work_date), 13) = 0 THEN 'EARLY_LEAVE'
        WHEN MOD(se.rn + DAY(wd.work_date), 11) = 0 THEN 'TARDY'
        ELSE 'NORMAL'
        END AS status,
    CASE
        WHEN MOD(se.rn + DAY(wd.work_date), 11) = 0 THEN '출근길 교통 정체'
        ELSE NULL
        END AS tardy_reason,
    CASE
        WHEN MOD(se.rn + DAY(wd.work_date), 17) = 0 THEN '고객사 방문으로 외근 처리'
        WHEN MOD(se.rn + DAY(wd.work_date), 13) = 0 THEN '개인 병원 진료 승인'
        ELSE NULL
        END AS modify_reason,
    CASE
        WHEN MOD(se.rn + DAY(wd.work_date), 5) = 0 THEN 1.5
        WHEN MOD(se.rn + DAY(wd.work_date), 9) = 0 THEN 0.5
        ELSE 0.0
        END AS overtime_hours,
    CASE
        WHEN MOD(se.rn + DAY(wd.work_date), 5) = 0 THEN 0.5
        ELSE 0.0
        END AS night_work_hours,
    0.0 AS holiday_work_hours,
    CASE
        WHEN MOD(se.rn + DAY(wd.work_date), 23) = 0 THEN TRUE
        ELSE FALSE
        END AS is_unpaid_leave,
    TRUE AS is_closed
FROM seed_employees se
         CROSS JOIN work_days wd
         LEFT JOIN attendance_record ar
                   ON ar.employee_id = se.employee_id
                       AND ar.work_date = wd.work_date
WHERE ar.attendance_id IS NULL;

INSERT INTO attendance_history (
    attendance_id,
    employee_id,
    actor_employee_id,
    action_type,
    reason,
    work_date,
    before_check_in_time,
    after_check_in_time,
    before_check_out_time,
    after_check_out_time,
    before_tardy_reason,
    after_tardy_reason,
    before_status,
    after_status,
    before_closed,
    after_closed,
    created_at
)
SELECT
    ar.attendance_id,
    ar.employee_id,
    1 AS actor_employee_id,
    CASE ar.status
        WHEN 'TARDY' THEN 'ADMIN_MODIFY'
        WHEN 'BUSINESS_TRIP' THEN 'BUSINESS_TRIP_APPROVED'
        WHEN 'VACATION' THEN 'LEAVE_APPROVED'
        ELSE 'DAY_CLOSE'
        END AS action_type,
    COALESCE(ar.modify_reason, ar.tardy_reason, '일마감 반영') AS reason,
    ar.work_date,
    NULL,
    ar.check_in_time,
    NULL,
    ar.check_out_time,
    NULL,
    ar.tardy_reason,
    'NORMAL',
    ar.status,
    FALSE,
    ar.is_closed,
    CONCAT(ar.work_date, ' 18:30:00') AS created_at
FROM attendance_record ar
         JOIN employee e ON e.employee_id = ar.employee_id
         LEFT JOIN attendance_history ah
                   ON ah.attendance_id = ar.attendance_id
                       AND ah.action_type = CASE ar.status
                                                WHEN 'TARDY' THEN 'ADMIN_MODIFY'
                                                WHEN 'BUSINESS_TRIP' THEN 'BUSINESS_TRIP_APPROVED'
                                                WHEN 'VACATION' THEN 'LEAVE_APPROVED'
                                                ELSE 'DAY_CLOSE'
                           END
WHERE e.email LIKE '%@seed.rhight.local'
  AND ar.work_date BETWEEN DATE('2026-03-02') AND DATE('2026-03-06')
  AND ah.history_id IS NULL;

INSERT INTO leave_request (
    employee_id,
    start_date,
    end_date,
    leave_type,
    leave_status,
    used_days,
    reason,
    reject_reason
)
SELECT
    e.employee_id,
    req.start_date,
    req.end_date,
    req.leave_type,
    req.leave_status,
    req.used_days,
    req.reason,
    req.reject_reason
FROM (
         SELECT 21 AS seq_no, DATE('2026-03-06') AS start_date, DATE('2026-03-06') AS end_date, 'ANNUAL' AS leave_type, 'APPROVED' AS leave_status, 1.0 AS used_days, '가족 행사 참석' AS reason, NULL AS reject_reason
UNION ALL
SELECT 34, DATE('2026-03-07'), DATE('2026-03-07'), 'HALF_PM', 'APPROVED', 0.5, '은행 방문', NULL
UNION ALL
SELECT 52, DATE('2026-03-10'), DATE('2026-03-11'), 'SPECIAL', 'PENDING', 2.0, '가족 돌봄', NULL
UNION ALL
SELECT 67, DATE('2026-03-14'), DATE('2026-03-14'), 'ANNUAL', 'REJECTED', 1.0, '개인 일정', '분기 마감 인력 부족'
UNION ALL
SELECT 83, DATE('2026-03-18'), DATE('2026-03-18'), 'HALF_AM', 'CANCELED', 0.5, '병원 예약', NULL
    ) req
    JOIN employee e ON e.email = CONCAT('employee', LPAD(req.seq_no, 3, '0'), '@seed.rhight.local')
    LEFT JOIN leave_request lr
    ON lr.employee_id = e.employee_id
    AND lr.start_date = req.start_date
    AND lr.end_date = req.end_date
    AND lr.leave_type = req.leave_type
WHERE lr.leave_request_id IS NULL;

INSERT INTO business_trip_request (
    employee_id,
    trip_type,
    destination,
    start_datetime,
    end_datetime,
    reason,
    approval_status,
    reject_reason
)
SELECT
    e.employee_id,
    req.trip_type,
    req.destination,
    req.start_datetime,
    req.end_datetime,
    req.reason,
    req.approval_status,
    req.reject_reason
FROM (
         SELECT 13 AS seq_no, 'BUSINESS_TRIP' AS trip_type, '부산 고객사' AS destination, '2026-03-12 09:00:00' AS start_datetime, '2026-03-13 18:00:00' AS end_datetime, '분기 구축 미팅' AS reason, 'APPROVED' AS approval_status, NULL AS reject_reason
         UNION ALL
         SELECT 28, 'OUTSIDE_WORK', '판교 데이터센터', '2026-03-11 13:00:00', '2026-03-11 17:00:00', '장비 점검', 'PENDING', NULL
         UNION ALL
         SELECT 46, 'OUTSIDE_WORK', '협력사 사무실', '2026-03-17 10:00:00', '2026-03-17 15:00:00', '연동 테스트', 'REJECTED', '보안 승인 누락'
         UNION ALL
         SELECT 72, 'BUSINESS_TRIP', '대전 지사', '2026-03-20 08:00:00', '2026-03-20 20:00:00', '워크숍 참석', 'CANCELED', NULL
     ) req
         JOIN employee e ON e.email = CONCAT('employee', LPAD(req.seq_no, 3, '0'), '@seed.rhight.local')
         LEFT JOIN business_trip_request btr
                   ON btr.employee_id = e.employee_id
                       AND btr.start_datetime = req.start_datetime
                       AND btr.end_datetime = req.end_datetime
WHERE btr.trip_id IS NULL;

INSERT INTO overtime_request (
    employee_id,
    work_date,
    start_time,
    end_time,
    reason,
    approval_status,
    reject_reason
)
SELECT
    e.employee_id,
    req.work_date,
    req.start_time,
    req.end_time,
    req.reason,
    req.approval_status,
    req.reject_reason
FROM (
         SELECT 17 AS seq_no, DATE('2026-03-04') AS work_date, '2026-03-04 18:30:00' AS start_time, '2026-03-04 21:00:00' AS end_time, '배포 안정화 작업' AS reason, 'APPROVED' AS approval_status, NULL AS reject_reason
UNION ALL
SELECT 39, DATE('2026-03-05'), '2026-03-05 18:30:00', '2026-03-05 20:30:00', '성능 튜닝', 'PENDING', NULL
UNION ALL
SELECT 58, DATE('2026-03-07'), '2026-03-07 19:00:00', '2026-03-07 21:00:00', '긴급 장애 대응', 'REJECTED', '사전 승인 누락'
UNION ALL
SELECT 81, DATE('2026-03-14'), '2026-03-14 18:00:00', '2026-03-14 19:30:00', '월간 마감 대응', 'CANCELED', NULL
    ) req
    JOIN employee e ON e.email = CONCAT('employee', LPAD(req.seq_no, 3, '0'), '@seed.rhight.local')
    LEFT JOIN overtime_request otr
    ON otr.employee_id = e.employee_id
    AND otr.work_date = req.work_date
    AND otr.start_time = req.start_time
WHERE otr.overtime_id IS NULL;

INSERT INTO weekly_work_schedule (
    employee_id,
    start_date,
    end_date,
    approval_status,
    plan_date,
    work_form,
    schedule_title,
    memo,
    reject_reason,
    created_at,
    updated_at
)
SELECT
    e.employee_id,
    req.start_date,
    req.end_date,
    req.approval_status,
    req.plan_date,
    req.work_form,
    req.schedule_title,
    req.memo,
    req.reject_reason,
    req.created_at,
    req.updated_at
FROM (
         SELECT 12 AS seq_no, '2026-03-09 00:00:00' AS start_date, '2026-03-15 23:59:59' AS end_date, 'APPROVED' AS approval_status, DATE('2026-03-10') AS plan_date, 'OFFICE' AS work_form, '백엔드 스프린트' AS schedule_title, '화요일 대면 협업' AS memo, NULL AS reject_reason, '2026-03-07 09:00:00' AS created_at, '2026-03-08 09:00:00' AS updated_at
UNION ALL
SELECT 31, '2026-03-09 00:00:00', '2026-03-15 23:59:59', 'PENDING', DATE('2026-03-11'), 'REMOTE', '집중 개발 데이', '코드리뷰 중심 일정', NULL, '2026-03-07 10:00:00', NULL
UNION ALL
SELECT 54, '2026-03-16 00:00:00', '2026-03-22 23:59:59', 'REJECTED', DATE('2026-03-18'), 'OFFSITE', '고객 미팅 주간', '수도권 외근 비중 높음', '현장 일정 증빙 부족', '2026-03-14 11:00:00', '2026-03-15 14:30:00'
UNION ALL
SELECT 77, '2026-03-16 00:00:00', '2026-03-22 23:59:59', 'CANCELED', DATE('2026-03-19'), 'OFFICE', '품질 점검 주간', '내부 사정으로 취소', NULL, '2026-03-14 13:00:00', '2026-03-15 16:00:00'
    ) req
    JOIN employee e ON e.email = CONCAT('employee', LPAD(req.seq_no, 3, '0'), '@seed.rhight.local')
    LEFT JOIN weekly_work_schedule wws
    ON wws.employee_id = e.employee_id
    AND wws.plan_date = req.plan_date
    AND wws.schedule_title = req.schedule_title
WHERE wws.weekly_id IS NULL;

-- ---------------------------------------------------------------------------
-- Payroll seed
-- ---------------------------------------------------------------------------
INSERT INTO insurance_rate (
    apply_year,
    national_pension_rate,
    health_insurance_rate,
    long_term_care_rate,
    emp_insurance_rate
)
SELECT
    seed.apply_year,
    seed.national_pension_rate,
    seed.health_insurance_rate,
    seed.long_term_care_rate,
    seed.emp_insurance_rate
FROM (
         SELECT 2025 AS apply_year, 0.04500 AS national_pension_rate, 0.03545 AS health_insurance_rate, 0.12950 AS long_term_care_rate, 0.00900 AS emp_insurance_rate
         UNION ALL
         SELECT 2026, 0.04500, 0.03545, 0.12950, 0.00900
     ) seed
         LEFT JOIN insurance_rate ir ON ir.apply_year = seed.apply_year
WHERE ir.insurance_id IS NULL;

INSERT INTO salary_setting (
    employee_id,
    base_salary,
    meal_allowance,
    apply_start_date,
    apply_end_date
)
SELECT
    e.employee_id,
    CASE r.rank_name
        WHEN '사장' THEN 12000000.00
        WHEN '부사장' THEN 9800000.00
        WHEN '전무' THEN 9000000.00
        WHEN '상무' THEN 8200000.00
        WHEN '이사' THEN 7200000.00
        WHEN '부장' THEN 6200000.00
        WHEN '차장' THEN 5400000.00
        WHEN '과장' THEN 4700000.00
        WHEN '대리' THEN 3900000.00
        WHEN '주임' THEN 3400000.00
        ELSE 3000000.00
        END
        + CASE j.job_name
              WHEN '백엔드 개발' THEN 350000.00
              WHEN '프론트엔드 개발' THEN 250000.00
              WHEN '모바일 개발' THEN 300000.00
              WHEN 'QA' THEN 180000.00
              WHEN '영업' THEN 220000.00
              WHEN '재무' THEN 150000.00
              WHEN '인사' THEN 120000.00
              ELSE 100000.00
        END AS base_salary,
    CASE
        WHEN p.position_name IN ('CEO', '본부장', '센터장', '실장') THEN 300000.00
        WHEN p.position_name IN ('팀장', '파트장') THEN 250000.00
        ELSE 200000.00
        END AS meal_allowance,
    DATE('2026-01-01') AS apply_start_date,
    NULL AS apply_end_date
FROM employee e
    JOIN employee_hr_info ehr ON ehr.employee_id = e.employee_id
    JOIN hr_rank r ON r.rank_id = ehr.rank_id
    JOIN hr_position p ON p.position_id = ehr.position_id
    JOIN job j ON j.job_id = ehr.job_id
    LEFT JOIN salary_setting ss
    ON ss.employee_id = e.employee_id
    AND ss.apply_start_date = DATE('2026-01-01')
WHERE e.email LIKE '%@seed.rhight.local'
  AND ss.id IS NULL;

INSERT INTO payroll_ledger (
    employee_id,
    insurance_id,
    target_month,
    salary_amount,
    overtime_amount,
    meal_amount,
    total_payment,
    net_pay,
    is_finalized,
    is_sent,
    national_pension_amount,
    health_insurance_amount,
    long_term_care_amount,
    emp_insurance_amount,
    income_tax_amount,
    local_tax_amount,
    employee_name_snapshot,
    dept_name_snapshot,
    position_name_snapshot,
    bank_name_snapshot,
    account_number_snapshot_enc,
    account_holder_snapshot
)
WITH month_seed AS (
    SELECT '2026-01' AS target_month, 0.0 AS overtime_multiplier
    UNION ALL
    SELECT '2026-02', 0.5
    UNION ALL
    SELECT '2026-03', 1.0
),
     payroll_base AS (
         SELECT
             e.employee_id,
             e.employee_name,
             e.bank_name,
             e.account_number_enc,
             o.org_name,
             p.position_name,
             ss.base_salary,
             ss.meal_allowance,
             ms.target_month,
             ROUND(
                     CASE
                         WHEN ms.target_month = '2026-03'
                             THEN COALESCE(SUM(ar.overtime_hours), 0) * 15000.00
                         ELSE ms.overtime_multiplier * 80000.00
                         END,
                     2
             ) AS overtime_amount
         FROM employee e
                  JOIN employee_hr_info ehr ON ehr.employee_id = e.employee_id
                  JOIN organization o ON o.org_id = ehr.org_id
                  JOIN hr_position p ON p.position_id = ehr.position_id
                  JOIN salary_setting ss
                       ON ss.employee_id = e.employee_id
                           AND ss.apply_start_date = DATE('2026-01-01')
    CROSS JOIN month_seed ms
    LEFT JOIN attendance_record ar
ON ar.employee_id = e.employee_id
    AND ms.target_month = '2026-03'
    AND DATE_FORMAT(ar.work_date, '%Y-%m') = ms.target_month
WHERE e.email LIKE '%@seed.rhight.local'
GROUP BY
    e.employee_id,
    e.employee_name,
    e.bank_name,
    e.account_number_enc,
    o.org_name,
    p.position_name,
    ss.base_salary,
    ss.meal_allowance,
    ms.target_month,
    ms.overtime_multiplier
    )
SELECT
    pb.employee_id,
    ir.insurance_id,
    pb.target_month,
    pb.base_salary AS salary_amount,
    pb.overtime_amount,
    pb.meal_allowance AS meal_amount,
    ROUND(pb.base_salary + pb.overtime_amount + pb.meal_allowance, 2) AS total_payment,
    ROUND(
            (pb.base_salary + pb.overtime_amount + pb.meal_allowance)
                - ROUND(pb.base_salary * ir.national_pension_rate, 2)
                - ROUND(pb.base_salary * ir.health_insurance_rate, 2)
                - ROUND(pb.base_salary * ir.health_insurance_rate * ir.long_term_care_rate, 2)
                - ROUND(pb.base_salary * ir.emp_insurance_rate, 2)
                - ROUND(pb.base_salary * 0.0320, 2)
                - ROUND(ROUND(pb.base_salary * 0.0320, 2) * 0.10, 2),
            2
    ) AS net_pay,
    'Y' AS is_finalized,
    CASE WHEN pb.target_month = '2026-03' THEN 'N' ELSE 'Y' END AS is_sent,
    ROUND(pb.base_salary * ir.national_pension_rate, 2) AS national_pension_amount,
    ROUND(pb.base_salary * ir.health_insurance_rate, 2) AS health_insurance_amount,
    ROUND(pb.base_salary * ir.health_insurance_rate * ir.long_term_care_rate, 2) AS long_term_care_amount,
    ROUND(pb.base_salary * ir.emp_insurance_rate, 2) AS emp_insurance_amount,
    ROUND(pb.base_salary * 0.0320, 2) AS income_tax_amount,
    ROUND(ROUND(pb.base_salary * 0.0320, 2) * 0.10, 2) AS local_tax_amount,
    pb.employee_name AS employee_name_snapshot,
    pb.org_name AS dept_name_snapshot,
    pb.position_name AS position_name_snapshot,
    pb.bank_name AS bank_name_snapshot,
    pb.account_number_enc AS account_number_snapshot_enc,
    pb.employee_name AS account_holder_snapshot
FROM payroll_base pb
         JOIN insurance_rate ir ON ir.apply_year = 2026
         LEFT JOIN payroll_ledger pl
                   ON pl.employee_id = pb.employee_id
                       AND pl.target_month = pb.target_month
WHERE pl.id IS NULL;


INSERT INTO app_view (view_code, view_name, view_desc)
VALUES ('MAIN', '메인 대시보드', '메인 화면'),
       ('NOTICE_LIST', '공지사항', '공지사항 목록'),
       ('ADMIN_MAIN', '관리자 메인', '관리자 대시보드'),
       ('ADMIN_EMPLOYEES', '사원 관리', '관리자 사원 관리 화면'),
       ('ADMIN_HR_CHANGE', '인사변동 관리', '관리자 인사변동 화면'),
       ('ADMIN_POLICIES', '규정 관리', '관리자 규정 화면'),
       ('ADMIN_KMS_PERMISSION_HISTORY', 'KMS 권한 이력(관리자)', '관리자 KMS 권한 이력 화면'),
       ('ADMIN_NOTICES', '공지 관리', '관리자 공지사항 관리 화면'),
       ('ADMIN_ATTENDANCE', '근태 관리(관리자)', '관리자 근태 화면'),
       ('ADMIN_SALARY', '급여 관리(관리자)', '관리자 급여 화면'),
       ('APPROVAL_MAIN', '전자결재 메인', '전자결재 메인 화면'),
       ('APPROVAL_DRAFT', '결재 작성', '전자결재 작성 화면'),
       ('APPROVAL_STATUS', '결재 상태', '전자결재 상태 화면'),
       ('APPROVAL_BOX', '결재함', '전자결재 결재함 화면'),
       ('APPROVAL_BOX_LIST', '결재함 목록', '전자결재 결재함 상세 목록 화면'),
       ('APPROVAL_REVIEW', '결재 검토', '전자결재 검토 화면'),
       ('HR_MYPAGE', '인사 마이페이지', '인사 개인 정보 화면'),
       ('HR_ORG', '조직/팀 조회', '인사 조직/팀 화면'),
       ('HR_ORGCHART', '조직도', '인사 조직도 화면'),
       ('HR_MEMBER_ATTENDANCE', '팀원 근태', '팀원 근태 조회 화면'),
       ('HR_MEMBER_GOAL', '팀원 목표', '팀원 목표 조회 화면'),
       ('PERFORMANCE', '성과관리', '성과관리 화면'),
       ('ATTENDANCE_MAIN', '근태 메인', '근태 메인 화면'),
       ('ATTENDANCE_RECORD', '출퇴근 기록', '근태 기록 화면'),
       ('ATTENDANCE_REQUEST', '근태 신청', '근태 신청 화면'),
       ('ATTENDANCE_HISTORY', '근태 이력', '근태 이력 화면'),
       ('ATTENDANCE_SCHEDULE', '근무 일정', '근무 일정 화면'),
       ('ATTENDANCE_VACATION', '휴가 관리', '휴가 관리 화면'),
       ('ATTENDANCE_TEAM', '팀 근태', '팀 근태 화면'),
       ('ATTENDANCE_MANAGE', '근태 승인/관리', '근태 승인/관리 화면'),
       ('ATTENDANCE_FLEXIBLE', '유연근무', '유연근무 화면')
ON DUPLICATE KEY UPDATE view_name = VALUES(view_name),
                        view_desc = VALUES(view_desc);

INSERT INTO role_view (view_id, role_id)
SELECT av.view_id, r.role_id
FROM app_view av
         JOIN role r ON r.role_code = 'EVALUATEE'
WHERE av.view_code IN ('MAIN', 'NOTICE_LIST', 'APPROVAL_MAIN', 'APPROVAL_DRAFT', 'APPROVAL_STATUS',
                       'APPROVAL_BOX', 'APPROVAL_BOX_LIST', 'HR_MYPAGE', 'HR_ORG', 'HR_ORGCHART',
                       'ATTENDANCE_MAIN', 'ATTENDANCE_RECORD', 'ATTENDANCE_HISTORY',
                       'ATTENDANCE_SCHEDULE', 'ATTENDANCE_VACATION')
ON DUPLICATE KEY UPDATE view_id = VALUES(view_id),
                        role_id = VALUES(role_id);

INSERT INTO role_view (view_id, role_id)
SELECT av.view_id, r.role_id
FROM app_view av
         JOIN role r ON r.role_code = 'EVALUATOR'
WHERE av.view_code IN ('MAIN', 'NOTICE_LIST', 'APPROVAL_MAIN', 'APPROVAL_DRAFT', 'APPROVAL_STATUS',
                       'APPROVAL_BOX', 'APPROVAL_BOX_LIST', 'APPROVAL_REVIEW', 'HR_MYPAGE', 'HR_ORG',
                       'HR_ORGCHART', 'HR_MEMBER_ATTENDANCE', 'HR_MEMBER_GOAL', 'PERFORMANCE',
                       'ATTENDANCE_MAIN', 'ATTENDANCE_RECORD', 'ATTENDANCE_HISTORY',
                       'ATTENDANCE_SCHEDULE', 'ATTENDANCE_VACATION', 'ATTENDANCE_TEAM')
ON DUPLICATE KEY UPDATE view_id = VALUES(view_id),
                        role_id = VALUES(role_id);

INSERT INTO role_view (view_id, role_id)
SELECT av.view_id, r.role_id
FROM app_view av
         JOIN role r ON r.role_code = 'HR_ADMIN_MASTER'
ON DUPLICATE KEY UPDATE view_id = VALUES(view_id),
                        role_id = VALUES(role_id);

INSERT INTO role_view (view_id, role_id)
SELECT av.view_id, r.role_id
FROM app_view av
         JOIN role r ON r.role_code = 'HR_ADMIN_BASIC'
WHERE av.view_code IN ('MAIN', 'NOTICE_LIST', 'ADMIN_MAIN', 'ADMIN_EMPLOYEES', 'ADMIN_HR_CHANGE',
                       'ADMIN_POLICIES', 'ADMIN_NOTICES', 'ADMIN_ATTENDANCE', 'HR_MYPAGE', 'HR_ORG',
                       'HR_ORGCHART', 'HR_MEMBER_ATTENDANCE', 'HR_MEMBER_GOAL', 'APPROVAL_MAIN',
                       'APPROVAL_DRAFT', 'APPROVAL_STATUS', 'APPROVAL_BOX', 'APPROVAL_BOX_LIST')
ON DUPLICATE KEY UPDATE view_id = VALUES(view_id),
                        role_id = VALUES(role_id);

INSERT INTO role_view (view_id, role_id)
SELECT av.view_id, r.role_id
FROM app_view av
         JOIN role r ON r.role_code = 'HR_ADMIN_PAYROLL'
WHERE av.view_code IN ('MAIN', 'NOTICE_LIST', 'ADMIN_MAIN', 'ADMIN_SALARY', 'HR_MYPAGE', 'HR_ORG',
                       'APPROVAL_MAIN', 'APPROVAL_DRAFT', 'APPROVAL_STATUS', 'APPROVAL_BOX',
                       'APPROVAL_BOX_LIST')
ON DUPLICATE KEY UPDATE view_id = VALUES(view_id),
                        role_id = VALUES(role_id);



COMMIT;
