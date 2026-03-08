package com.reverse.approval.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "overtime_detail")
@Comment("연장근무 양식 테이블")
public class OvertimeDetail extends ElectronicApproval {

    @Column(name = "work_date", nullable = false)
    @Comment("근무 일자")
    private LocalDate workDate;

    @Column(name = "start_time", nullable = false)
    @Comment("연장 시작 시간")
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    @Comment("연장 종료 시간")
    private LocalTime endTime;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    @Comment("사유")
    private String reason;
}
