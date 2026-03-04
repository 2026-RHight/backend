

# 근태 관리
CREATE TABLE attendance_record (
                                   attendance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   employee_id BIGINT NOT NULL,
                                   work_date DATE NOT NULL,
                                   check_in_time DATETIME NOT NULL,
                                   check_out_time DATETIME,
                                   status VARCHAR(20) NOT NULL COMMENT 'NORMAL(정상), TARDY(지각), EARLY_LEAVE(조퇴), ABSENT(결근), VACATION(휴가)',
                                   tardy_reason VARCHAR(255) COMMENT '지각 사유',
                                   modify_reason VARCHAR(255) COMMENT '관리자 수정 사유'
);

# 사원별 총 연차 관리
CREATE TABLE leave_balance (
                               employee_id BIGINT PRIMARY KEY,
                               total_annual_leave DECIMAL(5,1) NOT NULL DEFAULT 0.0 COMMENT '총 발생 연차 (0.5일 단위)'
);

# 휴가 신청 내역
CREATE TABLE leave_request (
                               leave_request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               employee_id BIGINT NOT NULL,
                               start_date DATE NOT NULL,
                               end_date DATE NOT NULL,
                               leave_type VARCHAR(20) NOT NULL COMMENT 'ANNUAL(연차), HALF_AM(오전반차), HALF_PM(오후반차), SPECIAL(특별휴가)',
                               leave_status VARCHAR(20) NOT NULL COMMENT 'PENDING(대기), APPROVED(승인), REJECTED(반려), CANCELED(취소)',
                               used_days DECIMAL(5,1) NOT NULL COMMENT '차감될 일수',
                               reason VARCHAR(255) NOT NULL COMMENT '휴가 사유',
                               reject_reason VARCHAR(255) COMMENT '관리자 반려 사유'
);

# 외근/출장 신청 내역
CREATE TABLE business_trip_request (
                                       trip_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       employee_id BIGINT NOT NULL,
                                       trip_type VARCHAR(20) NOT NULL COMMENT 'OUTSIDE_WORK(외근), BUSINESS_TRIP(출장)',
                                       destination VARCHAR(255) NOT NULL COMMENT '목적지',
                                       start_datetime DATETIME NOT NULL COMMENT '시작 일시',
                                       end_datetime DATETIME NOT NULL COMMENT '종료 일시',
                                       reason VARCHAR(255) NOT NULL COMMENT '신청 사유',
                                       approval_status VARCHAR(20) NOT NULL COMMENT 'PENDING(대기), APPROVED(승인), REJECTED(반려), CANCELED(취소)',
                                       reject_reason VARCHAR(255) COMMENT '관리자 반려 사유'
);

# 연장근무 신청 내역
CREATE TABLE overtime_request (
                                  overtime_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  employee_id BIGINT NOT NULL,
                                  work_date DATE NOT NULL COMMENT '근무 일자',
                                  start_time DATETIME NOT NULL COMMENT '연장근무 시작 시간',
                                  end_time DATETIME NOT NULL COMMENT '연장근무 종료 시간',
                                  reason VARCHAR(255) NOT NULL COMMENT '신청 사유',
                                  approval_status VARCHAR(20) NOT NULL COMMENT 'PENDING(대기), APPROVED(승인), REJECTED(반려), CANCELED(취소)',
                                  reject_reason VARCHAR(255) COMMENT '관리자 반려 사유'
);

#  ==========================================
#  테스트용 더미 데이터 (필요시 주석 해제하여 사용)
#  INSERT INTO leave_balance (employee_id, total_annual_leave) VALUES (1, 15.0);
#  INSERT INTO leave_balance (employee_id, total_annual_leave) VALUES (2, 20.0);