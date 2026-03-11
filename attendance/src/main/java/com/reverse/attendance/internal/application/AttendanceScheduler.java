package com.reverse.attendance.internal.application;

import com.reverse.attendance.internal.persistence.LeaveMapper;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

    private final AttendanceSyncService attendanceSyncService;
    private final LeaveMapper leaveMapper;

    /** 매일 자정(0시 0분 0초)에 실행 전날 출근은 했으나 퇴근을 찍지 않은 사원들을 EARLY_LEAVE(조퇴 등)로 자동 마감 처리하는 스케줄러. */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void autoCloseMissingCheckOuts() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("[System Batch] 퇴근 미처리자 자정 자동 마감 처리 시작: 기준일자 {}", yesterday);

        try {
            int updatedRows =
                    attendanceSyncService.autoCloseMissingCheckOutsByDate(
                            yesterday, "System Auto Closed", "DAILY_AUTO_CLOSE");
            log.info("[System Batch] 퇴근 미처리자 자정 자동 마감 처리 완료: 총 {}건 마감됨", updatedRows);
        } catch (Exception e) {
            log.error("[System Batch] 퇴근 미처리자 자정 마감 처리 중 오류 발생: ", e);
        }
    }

    /** 매년 1월 1일 자정(0시 0분 10초)에 실행 재직 중인 사원들을 대상으로 당해 년도 연차를 일괄 지급하는 스케줄러. */
    @Scheduled(cron = "10 0 0 1 1 *", zone = "Asia/Seoul")
    public void grantAnnualLeaveForNewYear() {
        int year = LocalDate.now().getYear();
        log.info("[System Batch] 신년 연차 일괄 부여 배치 시작: 기준년도 {}", year);

        try {
            int insertedRows = leaveMapper.insertNextYearLeaveBalance(year);
            log.info("[System Batch] 신년 연차 일괄 부여 배치 완료: 총 {}명에게 연차 생성됨", insertedRows);
        } catch (Exception e) {
            log.error("[System Batch] 신년 연차 일괄 부여 배치 중 오류 발생: ", e);
        }
    }
}
