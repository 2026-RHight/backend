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
