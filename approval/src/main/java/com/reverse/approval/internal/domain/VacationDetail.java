package com.reverse.approval.internal.domain;

import com.reverse.approval.internal.domain.enums.VacationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "vacation_detail")
public class VacationDetail extends ElectronicApproval {

    @Column(name = "vacation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Comment("휴가 종류")
    private VacationType vacationType;

    @Column(name = "start_dt", nullable = false)
    @Comment("시작일")
    private LocalDateTime startDate;

    @Column(name = "end_dt", nullable = false)
    @Comment("종료일")
    private LocalDateTime endDate;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    @Comment("사유")
    private String reason;
}
