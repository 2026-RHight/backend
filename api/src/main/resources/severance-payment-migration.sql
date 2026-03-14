-- 퇴직금 지급 처리 저장용 테이블 추가

CREATE TABLE IF NOT EXISTS severance_payment (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    retirement_date DATE NOT NULL,
    service_days BIGINT NOT NULL,
    service_years DECIMAL(10,4) NOT NULL DEFAULT 0.0000,
    average_monthly_wage DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    estimated_severance_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    paid_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    payment_date DATE NOT NULL,
    bank_name_snapshot VARCHAR(50),
    account_number_snapshot_enc TEXT,
    account_holder_snapshot VARCHAR(100),
    note TEXT,
    paid_by_employee_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_severance_payment_employee_retirement (employee_id, retirement_date),
    KEY idx_severance_payment_date (payment_date),
    CONSTRAINT fk_severance_payment_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id),
    CONSTRAINT fk_severance_payment_paid_by FOREIGN KEY (paid_by_employee_id) REFERENCES employee(employee_id)
);
