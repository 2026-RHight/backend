package com.reverse.approval.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "business_trip_detail")
@Comment("외근 및 출장 양식 테이블")
public class BusinessTripDetail extends ElectronicApproval {

    @Column(name = "trip_type", nullable = false, length = 100)
    @Comment("외근/출장 유형")
    private String tripType;

    @Column(name = "destination", nullable = false, length = 100)
    @Comment("목적지")
    private String destination;

    @Column(name = "start_dt", nullable = false)
    @Comment("시작 일시")
    private LocalDateTime startDate;

    @Column(name = "end_dt", nullable = false)
    @Comment("종료 일시")
    private LocalDateTime endDate;

    @Column(name = "reason", nullable = false)
    @Comment("사유")
    private String reason;
}
