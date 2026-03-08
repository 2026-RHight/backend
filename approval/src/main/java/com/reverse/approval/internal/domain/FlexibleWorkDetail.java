package com.reverse.approval.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "flexible_work_detail")
@Comment("유연근무 양식 테이블")
public class FlexibleWorkDetail extends ElectronicApproval {

    @Column(name = "start_dt", nullable = false)
    @Comment("시작일")
    private LocalDateTime startDate;

    @Column(name = "end_dt", nullable = false)
    @Comment("종료일")
    private LocalDateTime endDate;

    @Column(name = "reason", nullable = false)
    @Comment("사유")
    private String reason;
}
