CREATE TABLE IF NOT EXISTS hr_file (
                                    hr_file_id BIGINT NOT NULL AUTO_INCREMENT,
                                    file_url TEXT NOT NULL,
                                    file_title VARCHAR(255) NULL,
    PRIMARY KEY (hr_file_id)
    );

CREATE TABLE IF NOT EXISTS employee (
                                        employee_id BIGINT NOT NULL AUTO_INCREMENT,
                                        employee_num VARCHAR(50) NOT NULL,
    employee_name VARCHAR(100) NOT NULL,
    employee_password VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    ext VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL,
    bank_name VARCHAR(50) NOT NULL,
    account_number_enc TEXT NOT NULL,
    account_number_hash VARCHAR(64) NOT NULL,
    resident_number_enc TEXT NOT NULL,
    resident_number_hash VARCHAR(64) NOT NULL,
    initial_state BOOLEAN NOT NULL,
    employ_state ENUM('WORK','LEAVE','RESIGN') NOT NULL,
    hire_date DATE NOT NULL,
    profile_id BIGINT NOT NULL,
    PRIMARY KEY (employee_id),
    UNIQUE KEY uk_employee_employee_num (employee_num),
    CONSTRAINT fk_employee_profile FOREIGN KEY (profile_id) REFERENCES hr_file(hr_file_id)
    );

CREATE TABLE IF NOT EXISTS role (
                                    role_id BIGINT NOT NULL AUTO_INCREMENT,
                                    role_code VARCHAR(50) NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_role_role_code (role_code)
    );

CREATE TABLE IF NOT EXISTS employee_role (
                                            employee_id BIGINT NOT NULL,
                                            role_id BIGINT NOT NULL,
                                            PRIMARY KEY (employee_id, role_id),
    CONSTRAINT fk_employee_role_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id),
    CONSTRAINT fk_employee_role_role FOREIGN KEY (role_id) REFERENCES role(role_id)
    );

CREATE TABLE IF NOT EXISTS password_history (
                                                id BIGINT NOT NULL AUTO_INCREMENT,
                                                employee_id BIGINT NOT NULL,
                                                password_hash VARCHAR(255) NOT NULL,
    change_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_password_history_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
    );

CREATE TABLE IF NOT EXISTS organization (
                                            org_id BIGINT NOT NULL AUTO_INCREMENT,
                                            org_name VARCHAR(255) NOT NULL,
    org_type ENUM('COMPANY','HEADQUARTER','CENTER','DIVISION','DEPARTMENT','TEAM') NOT NULL,
    parent_org_id BIGINT NULL,
    org_level TINYINT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (org_id),
    KEY idx_org_parent (parent_org_id),
    KEY idx_org_type (org_type),
    CONSTRAINT fk_organization_parent FOREIGN KEY (parent_org_id) REFERENCES organization(org_id)
    );



CREATE TABLE IF NOT EXISTS org_closure (
    ancestor_org_id BIGINT NOT NULL,
    descendant_org_id BIGINT NOT NULL,
    depth INT NOT NULL,
    sort_path VARCHAR(2000) NULL,
    PRIMARY KEY (ancestor_org_id, descendant_org_id),
    KEY idx_org_closure_descendant (descendant_org_id),
    KEY idx_org_closure_ancestor_depth (ancestor_org_id, depth),
    CONSTRAINT fk_org_closure_ancestor FOREIGN KEY (ancestor_org_id) REFERENCES organization(org_id),
    CONSTRAINT fk_org_closure_descendant FOREIGN KEY (descendant_org_id) REFERENCES organization(org_id)
);

CREATE TABLE IF NOT EXISTS hr_position (
    position_id BIGINT NOT NULL AUTO_INCREMENT,
    position_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (position_id)
    );

CREATE TABLE IF NOT EXISTS hr_rank (
    rank_id BIGINT NOT NULL AUTO_INCREMENT,
    rank_name VARCHAR(255) NOT NULL,
    rank_no BIGINT NULL,
    PRIMARY KEY (rank_id)
    );

CREATE TABLE IF NOT EXISTS job (
                                job_id BIGINT NOT NULL AUTO_INCREMENT,
                                job_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (job_id)
    );

CREATE TABLE IF NOT EXISTS working_area (
                                            area_id BIGINT NOT NULL AUTO_INCREMENT,
                                            area_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (area_id)
    );

CREATE TABLE IF NOT EXISTS employee_hr_info (
                                                employee_hr_id BIGINT NOT NULL AUTO_INCREMENT,
                                                employee_id BIGINT NOT NULL,
                                                org_id BIGINT NOT NULL,
                                                effective_from DATE NOT NULL,
                                                position_id BIGINT NOT NULL,
                                                rank_id BIGINT NOT NULL,
                                                job_id BIGINT NOT NULL,
                                                employ_type ENUM('REGULAR','NON_REGULAR','CONTRACT') NULL,
    recruit_type ENUM('NEW','EXPERIENCED') NULL,
    area_id BIGINT NOT NULL,
    PRIMARY KEY (employee_hr_id),
    UNIQUE KEY uk_employee_hr_info_employee_id (employee_id),
    KEY idx_employee_hr_employee (employee_id),
    KEY idx_employee_hr_effective_from (effective_from),
    CONSTRAINT fk_employee_hr_info_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id),
    CONSTRAINT fk_employee_hr_info_org FOREIGN KEY (org_id) REFERENCES organization(org_id),
    CONSTRAINT fk_employee_hr_info_position FOREIGN KEY (position_id) REFERENCES hr_position(position_id),
    CONSTRAINT fk_employee_hr_info_rank FOREIGN KEY (rank_id) REFERENCES hr_rank(rank_id),
    CONSTRAINT fk_employee_hr_info_job FOREIGN KEY (job_id) REFERENCES job(job_id),
    CONSTRAINT fk_employee_hr_info_area FOREIGN KEY (area_id) REFERENCES working_area(area_id)
    );

-- ==========================================
-- 본인(HEAD)이 작성한 근태 관리 관련 테이블
-- ==========================================

-- 근태 관리
CREATE TABLE IF NOT EXISTS attendance_record (
                                                attendance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                employee_id BIGINT NOT NULL,
                                                work_date DATE NOT NULL,
                                                check_in_time TIME,
                                                check_out_time TIME,
                                                status VARCHAR(20) NOT NULL COMMENT 'NORMAL(정상), TARDY(지각), EARLY_LEAVE(조퇴), ABSENT(결근), VACATION(휴가)',
    tardy_reason VARCHAR(255) COMMENT '지각 사유',
    modify_reason VARCHAR(255) COMMENT '관리자 수정 사유',
    CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id),
    UNIQUE KEY uk_attendance_employee_date (employee_id, work_date)
    );

-- 사원별 총 연차 관리
CREATE TABLE IF NOT EXISTS leave_balance (
                                            employee_id BIGINT PRIMARY KEY,
                                            total_annual_leave DECIMAL(5,1) NOT NULL DEFAULT 0.0 COMMENT '총 발생 연차 (0.5일 단위)',
    CONSTRAINT fk_leave_balance_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
    );

-- 휴가 신청 내역
CREATE TABLE IF NOT EXISTS leave_request (
                                            leave_request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            employee_id BIGINT NOT NULL,
                                            start_date DATE NOT NULL,
                                            end_date DATE NOT NULL,
                                            leave_type VARCHAR(20) NOT NULL COMMENT 'ANNUAL(연차), HALF_AM(오전반차), HALF_PM(오후반차), SPECIAL(특별휴가)',
    leave_status VARCHAR(20) NOT NULL COMMENT 'PENDING(대기), APPROVED(승인), REJECTED(반려), CANCELED(취소)',
    used_days DECIMAL(5,1) NOT NULL COMMENT '차감될 일수',
    reason VARCHAR(255) NOT NULL COMMENT '휴가 사유',
    reject_reason VARCHAR(255) COMMENT '관리자 반려 사유',
    CONSTRAINT fk_leave_request_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
    );

-- 외근/출장 신청 내역
CREATE TABLE IF NOT EXISTS business_trip_request (
                                                    trip_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                    employee_id BIGINT NOT NULL,
                                                    trip_type VARCHAR(20) NOT NULL COMMENT 'OUTSIDE_WORK(외근), BUSINESS_TRIP(출장)',
    destination VARCHAR(255) NOT NULL COMMENT '목적지',
    start_datetime DATETIME NOT NULL COMMENT '시작 일시',
    end_datetime DATETIME NOT NULL COMMENT '종료 일시',
    reason VARCHAR(255) NOT NULL COMMENT '신청 사유',
    approval_status VARCHAR(20) NOT NULL COMMENT 'PENDING(대기), APPROVED(승인), REJECTED(반려), CANCELED(취소)',
    reject_reason VARCHAR(255) COMMENT '관리자 반려 사유',
    CONSTRAINT fk_business_trip_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
    );

-- 연장근무 신청 내역
CREATE TABLE IF NOT EXISTS overtime_request (
                                                overtime_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                employee_id BIGINT NOT NULL,
                                                work_date DATE NOT NULL COMMENT '근무 일자',
                                                start_time DATETIME NOT NULL COMMENT '연장근무 시작 시간',
                                                end_time DATETIME NOT NULL COMMENT '연장근무 종료 시간',
                                                reason VARCHAR(255) NOT NULL COMMENT '신청 사유',
    approval_status VARCHAR(20) NOT NULL COMMENT 'PENDING(대기), APPROVED(승인), REJECTED(반려), CANCELED(취소)',
    reject_reason VARCHAR(255) COMMENT '관리자 반려 사유',
    CONSTRAINT fk_overtime_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
    );

-- 유연근무 신청 내역
CREATE TABLE IF NOT EXISTS weekly_work_schedule (
    weekly_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    approval_status VARCHAR(20) NOT NULL COMMENT 'PENDING(대기), APPROVED(승인), REJECTED(반려), CANCELED(취소)',
    plan_date DATE NOT NULL COMMENT '근무 계획 일자',
    work_form VARCHAR(50) NOT NULL COMMENT 'OFFICE, REMOTE 등',
    schedule_title VARCHAR(255) NOT NULL,
    memo TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_weekly_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
    );


-- 팀원이 작성한 자격증/경력 관련 테이블


CREATE TABLE IF NOT EXISTS employee_skill_credential (
                                                        skill_id BIGINT NOT NULL AUTO_INCREMENT,
                                                        employee_id BIGINT NOT NULL,
                                                        category ENUM('CERTIFICATE','LANGUAGE','LICENSE','ETC') NOT NULL,
    skill_name VARCHAR(255) NOT NULL,
    acquisition_date DATE NOT NULL,
    license_number VARCHAR(255) NULL,
    hr_file_id BIGINT NOT NULL,
    PRIMARY KEY (skill_id),
    KEY idx_skill_employee (employee_id),
    CONSTRAINT fk_skill_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id),
    CONSTRAINT fk_skill_file FOREIGN KEY (hr_file_id) REFERENCES hr_file(hr_file_id)
    );

CREATE TABLE IF NOT EXISTS employee_career_details (
                                                    career_id BIGINT NOT NULL AUTO_INCREMENT,
                                                    employee_id BIGINT NOT NULL,
                                                    company_name VARCHAR(255) NOT NULL,
    org_name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    hr_file_id BIGINT NOT NULL,
    PRIMARY KEY (career_id),
    KEY idx_career_employee (employee_id),
    CONSTRAINT fk_career_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id),
    CONSTRAINT fk_career_file FOREIGN KEY (hr_file_id) REFERENCES hr_file(hr_file_id)
    );

CREATE TABLE IF NOT EXISTS hr_event (
    hr_event_id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    event_type ENUM('PROMOTION','TRANSFER','STATE_CHANGE','POSITION_CHANGE','ORG_CHANGE') NOT NULL,
    event_title VARCHAR(255) NOT NULL,
    requested_at DATETIME NULL,
    approved_at DATETIME NULL,
    effective_from DATE NOT NULL,
    effective_to DATE NULL,
    excuse VARCHAR(255) NULL,
    before_change JSON NULL,
    after_change JSON NULL,
    PRIMARY KEY (hr_event_id),
    KEY idx_hr_event_employee (employee_id),
    KEY idx_hr_event_type (event_type),
    KEY idx_hr_event_effective_from (effective_from),
    CONSTRAINT fk_hr_event_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- ==========================================
-- 급여(Payroll) 모듈 테이블
-- ==========================================

CREATE TABLE IF NOT EXISTS salary_setting (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    base_salary DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    meal_allowance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    apply_start_date DATE,
    apply_end_date DATE,
    CONSTRAINT fk_salary_setting_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

CREATE TABLE IF NOT EXISTS insurance_rate (
    insurance_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    apply_year INT NOT NULL,
    national_pension_rate DECIMAL(7,5) NOT NULL DEFAULT 0.04500,
    health_insurance_rate DECIMAL(7,5) NOT NULL DEFAULT 0.03545,
    long_term_care_rate DECIMAL(7,5) NOT NULL DEFAULT 0.00459,
    emp_insurance_rate DECIMAL(7,5) NOT NULL DEFAULT 0.00900,
    UNIQUE KEY uk_insurance_rate_year (apply_year)
);

CREATE TABLE IF NOT EXISTS payroll_ledger (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    insurance_id BIGINT,
    year_month VARCHAR(7) NOT NULL, -- e.g., '2024-03'
    salary_amount DECIMAL(15,2) DEFAULT 0.00,
    overtime_amount DECIMAL(15,2) DEFAULT 0.00,
    meal_amount DECIMAL(15,2) DEFAULT 0.00,
    total_payment DECIMAL(15,2) DEFAULT 0.00,
    net_pay DECIMAL(15,2) DEFAULT 0.00,
    is_finalized CHAR(1) DEFAULT 'N',
    is_sent CHAR(1) DEFAULT 'N',
    national_pension_amount DECIMAL(15,2) DEFAULT 0.00,
    health_insurance_amount DECIMAL(15,2) DEFAULT 0.00,
    long_term_care_amount DECIMAL(15,2) DEFAULT 0.00,
    emp_insurance_amount DECIMAL(15,2) DEFAULT 0.00,
    income_tax_amount DECIMAL(15,2) DEFAULT 0.00,
    local_tax_amount DECIMAL(15,2) DEFAULT 0.00,
    UNIQUE KEY uk_payroll_ledger_employee_month (employee_id, year_month),
    KEY idx_payroll_ledger_year_month (year_month),
    CONSTRAINT fk_payroll_ledger_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id),
    CONSTRAINT fk_payroll_ledger_insurance FOREIGN KEY (insurance_id) REFERENCES insurance_rate(insurance_id)
);

CREATE TABLE IF NOT EXISTS sequence_doc (
    id BIGINT NOT NULL AUTO_INCREMENT,
    prefix VARCHAR(3) NOT NULL,
    doc_year VARCHAR(4) NOT NULL,
    last_doc INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sequence_doc_prefix_year (prefix, doc_year)
);

CREATE TABLE IF NOT EXISTS event_publication (
    id CHAR(36) NOT NULL,
    event_type VARCHAR(512) NOT NULL,
    listener_id VARCHAR(512) NOT NULL,
    publication_date DATETIME(6) NOT NULL,
    completion_date DATETIME(6) NULL,
    serialized_event TEXT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS electronic_approval (
    approval_id BIGINT NOT NULL AUTO_INCREMENT,
    doc_type VARCHAR(31) NULL,
    doc_id CHAR(13) NULL,
    title VARCHAR(1000) NOT NULL,
    approval_status ENUM('TEMP','PENDING','HOLD','DELEGATED','COMPLETE','REJECTED','WITHDRAWN') NOT NULL,
    draft_dt DATETIME NOT NULL,
    drafter_id BIGINT NOT NULL,
    drafter_name VARCHAR(100) NOT NULL,
    department_name VARCHAR(255) NOT NULL,
    approve_dt DATETIME NULL,
    read_dt DATETIME NULL,
    PRIMARY KEY (approval_id),
    UNIQUE KEY uk_electronic_approval_doc_id (doc_id)
);

CREATE TABLE IF NOT EXISTS approval_line (
    approval_line_id BIGINT NOT NULL AUTO_INCREMENT,
    approval_seq TINYINT NOT NULL,
    approval_status ENUM('TEMP','PENDING','HOLD','DELEGATED','COMPLETE','REJECTED','WITHDRAWN') NOT NULL,
    reason VARCHAR(1000) NULL,
    approved_dt DATETIME NULL,
    approval_id BIGINT NOT NULL,
    approver_id BIGINT NOT NULL,
    approver_name VARCHAR(100) NOT NULL,
    approver_rank VARCHAR(255) NOT NULL,
    read_dt DATETIME NULL,
    PRIMARY KEY (approval_line_id),
    KEY idx_approval_line_approval_id (approval_id),
    UNIQUE KEY uk_approval_line_approval_seq (approval_id, approval_seq),
    CONSTRAINT fk_approval_line_approval FOREIGN KEY (approval_id) REFERENCES electronic_approval(approval_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS approval_attachment (
    file_id BIGINT NOT NULL AUTO_INCREMENT,
    file_key VARCHAR(512) NOT NULL,
    file_path VARCHAR(1024) NOT NULL,
    original_name VARCHAR(512) NOT NULL,
    created_dt DATETIME NOT NULL,
    approval_id BIGINT NOT NULL,
    PRIMARY KEY (file_id),
    KEY idx_approval_attachment_approval_id (approval_id),
    CONSTRAINT fk_approval_attachment_approval FOREIGN KEY (approval_id) REFERENCES electronic_approval(approval_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS recipient_line (
    recipient_id BIGINT NOT NULL AUTO_INCREMENT,
    approval_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_rank VARCHAR(255) NOT NULL,
    read_dt DATETIME NULL,
    PRIMARY KEY (recipient_id),
    KEY idx_recipient_line_approval_id (approval_id),
    UNIQUE KEY uk_recipient_line_approval_receiver (approval_id, receiver_id),
    CONSTRAINT fk_recipient_line_approval FOREIGN KEY (approval_id) REFERENCES electronic_approval(approval_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reference_line (
    reference_id BIGINT NOT NULL AUTO_INCREMENT,
    approval_id BIGINT NOT NULL,
    referencer_id BIGINT NOT NULL,
    referencer_name VARCHAR(100) NOT NULL,
    reference_rank VARCHAR(255) NOT NULL,
    read_dt DATETIME NULL,
    PRIMARY KEY (reference_id),
    KEY idx_reference_line_approval_id (approval_id),
    UNIQUE KEY uk_reference_line_approval_referencer (approval_id, referencer_id),
    CONSTRAINT fk_reference_line_approval FOREIGN KEY (approval_id) REFERENCES electronic_approval(approval_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS vacation_detail (
    approval_id BIGINT NOT NULL,
    vacation_type ENUM('ANNUAL','HALF','SICK','ETC') NOT NULL,
    start_dt DATETIME NOT NULL,
    end_dt DATETIME NOT NULL,
    reason TEXT NOT NULL,
    PRIMARY KEY (approval_id),
    CONSTRAINT fk_vacation_detail_approval FOREIGN KEY (approval_id) REFERENCES electronic_approval(approval_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS overtime_detail (
    approval_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    reason TEXT NOT NULL,
    PRIMARY KEY (approval_id),
    CONSTRAINT fk_overtime_detail_approval FOREIGN KEY (approval_id) REFERENCES electronic_approval(approval_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS flexible_work_detail (
    approval_id BIGINT NOT NULL,
    start_dt DATETIME NOT NULL,
    end_dt DATETIME NOT NULL,
    reason VARCHAR(255) NOT NULL,
    PRIMARY KEY (approval_id),
    CONSTRAINT fk_flexible_work_detail_approval FOREIGN KEY (approval_id) REFERENCES electronic_approval(approval_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS business_trip_detail (
    approval_id BIGINT NOT NULL,
    trip_type VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    start_dt DATETIME NOT NULL,
    end_dt DATETIME NOT NULL,
    reason VARCHAR(255) NOT NULL,
    PRIMARY KEY (approval_id),
    CONSTRAINT fk_business_trip_detail_approval FOREIGN KEY (approval_id) REFERENCES electronic_approval(approval_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS leave_detail (
    approval_id BIGINT NOT NULL,
    start_dt DATETIME NOT NULL,
    end_dt DATETIME NOT NULL,
    leave_type ENUM('PARENTAL_LEAVE','SICK_LEAVE','FAMILY_CARE_LEAVE') NOT NULL,
    reason TEXT NOT NULL,
    PRIMARY KEY (approval_id),
    CONSTRAINT fk_leave_detail_approval FOREIGN KEY (approval_id) REFERENCES electronic_approval(approval_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS rtw_detail (
    approval_id BIGINT NOT NULL,
    rtw_date DATE NOT NULL,
    reason TEXT NOT NULL,
    PRIMARY KEY (approval_id),
    CONSTRAINT fk_rtw_detail_approval FOREIGN KEY (approval_id) REFERENCES electronic_approval(approval_id) ON DELETE CASCADE
);

-- ==========================================
-- 성과(Performance) 모듈 테이블
-- ==========================================

DROP TABLE IF EXISTS peer_review;
DROP TABLE IF EXISTS team_evaluation;
DROP TABLE IF EXISTS performance_attachment;
DROP TABLE IF EXISTS monthly_performance;
DROP TABLE IF EXISTS evaluation;
DROP TABLE IF EXISTS performance_personal;
DROP TABLE IF EXISTS performance_team;
DROP TABLE IF EXISTS performance;

CREATE TABLE performance (
    performance_id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    work_item ENUM('PERSONAL','TEAM') NOT NULL,
    start_date DATE NULL,
    expected_end_date DATE NULL,
    end_date DATE NULL,
    work_detail TEXT NULL,
    status ENUM('WAITING','ACTIVE','ENDED') NOT NULL DEFAULT 'ACTIVE',
    achievement_rate INT NOT NULL DEFAULT 0,
    difficulty_score INT NOT NULL DEFAULT 5,
    comment TEXT NULL,
    feedback TEXT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    PRIMARY KEY (performance_id),
    KEY idx_performance_employee (employee_id),
    KEY idx_performance_status (status),
    KEY idx_performance_end_date (end_date),
    CONSTRAINT fk_performance_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

CREATE TABLE performance_personal (
    performance_id BIGINT NOT NULL,
    expected_value TEXT NULL,
    result_summary TEXT NULL,
    growth_point TEXT NULL,
    improvement TEXT NULL,
    PRIMARY KEY (performance_id),
    CONSTRAINT fk_performance_personal_performance FOREIGN KEY (performance_id) REFERENCES performance(performance_id) ON DELETE CASCADE
);

CREATE TABLE performance_team (
    performance_id BIGINT NOT NULL,
    weight INT NULL,
    team_result_summary TEXT NULL,
    special_point TEXT NULL,
    PRIMARY KEY (performance_id),
    CONSTRAINT fk_performance_team_performance FOREIGN KEY (performance_id) REFERENCES performance(performance_id) ON DELETE CASCADE
);

CREATE TABLE performance_attachment (
    attachment_id BIGINT NOT NULL AUTO_INCREMENT,
    performance_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    confirmed_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (attachment_id),
    KEY idx_performance_attachment_performance (performance_id),
    CONSTRAINT fk_performance_attachment_performance FOREIGN KEY (performance_id) REFERENCES performance(performance_id) ON DELETE CASCADE
);

CREATE TABLE evaluation (
    eval_id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    approval_id BIGINT NOT NULL,
    year INT NOT NULL,
    evaluation_score INT NULL,
    confirmed_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    PRIMARY KEY (eval_id),
    KEY idx_evaluation_employee (employee_id),
    KEY idx_evaluation_approval (approval_id),
    UNIQUE KEY uk_evaluation_employee_year_approval (employee_id, year, approval_id),
    CONSTRAINT fk_evaluation_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id),
    CONSTRAINT fk_evaluation_approval_employee FOREIGN KEY (approval_id) REFERENCES employee(employee_id)
);

CREATE TABLE monthly_performance (
    monthly_performance_id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    score INT NOT NULL,
    calculated_at DATETIME NOT NULL,
    PRIMARY KEY (monthly_performance_id),
    UNIQUE KEY uk_monthly_performance_employee_year_month (employee_id, year, month),
    KEY idx_monthly_performance_employee (employee_id),
    CONSTRAINT fk_monthly_performance_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

CREATE TABLE peer_review (
    peer_review_id BIGINT NOT NULL AUTO_INCREMENT,
    eval_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    communication_score INT NULL,
    solving_score INT NULL,
    responsibility_score INT NULL,
    team_contribution INT NULL,
    culture_contribution INT NULL,
    comment TEXT NULL,
    eval_year INT NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (peer_review_id),
    KEY idx_peer_review_eval (eval_id),
    KEY idx_peer_review_reviewer (reviewer_id),
    CONSTRAINT fk_peer_review_evaluation FOREIGN KEY (eval_id) REFERENCES evaluation(eval_id) ON DELETE CASCADE,
    CONSTRAINT fk_peer_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES employee(employee_id)
);

CREATE TABLE team_evaluation (
    team_evaluation_id BIGINT NOT NULL AUTO_INCREMENT,
    evaluator_id BIGINT NOT NULL,
    appraisee_id BIGINT NOT NULL,
    performance_eval TEXT NULL,
    work_attitude_eval TEXT NULL,
    teamwork_eval TEXT NULL,
    solving_eval TEXT NULL,
    performance_score INT NULL,
    performance_comment TEXT NULL,
    attitude_score INT NULL,
    attitude_comment TEXT NULL,
    collaboration_score INT NULL,
    collaboration_comment TEXT NULL,
    creativity_score INT NULL,
    creativity_comment TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    PRIMARY KEY (team_evaluation_id),
    KEY idx_team_evaluation_evaluator (evaluator_id),
    KEY idx_team_evaluation_appraisee (appraisee_id),
    UNIQUE KEY uk_team_evaluation_evaluator_appraisee (evaluator_id, appraisee_id),
    CONSTRAINT fk_team_evaluation_evaluator FOREIGN KEY (evaluator_id) REFERENCES employee(employee_id),
    CONSTRAINT fk_team_evaluation_appraisee FOREIGN KEY (appraisee_id) REFERENCES employee(employee_id)
);
