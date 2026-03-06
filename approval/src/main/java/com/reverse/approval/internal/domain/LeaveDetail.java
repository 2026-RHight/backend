package com.reverse.approval.internal.domain;

import com.reverse.approval.internal.domain.enums.LeaveType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "leave_detail")
@Comment("휴직 양식 테이블")
public class LeaveDetail extends ElectronicApproval {

    @Column(name = "start_dt", nullable = false)
    @Comment("시작일")
    private LocalDateTime startDate;

    @Column(name = "end_dt", nullable = false)
    @Comment("종료일")
    private LocalDateTime endDate;

    @Column(name = "leave_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Comment("휴직 종류")
    private LeaveType leaveType;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    @Comment("사유")
    private String reason;
}
