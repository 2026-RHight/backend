package com.reverse.approval.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;

@Entity
@Table(name = "rtw_detail")
public class RTWDetail extends ElectronicApproval {
    @Column(name = "rtw_date", nullable = false)
    @Comment("종료일")
    private LocalDate RTWDate;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    @Comment("사유")
    private String reason;
}
