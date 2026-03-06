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
