package com.reverse.approval.internal.domain;

import com.reverse.approval.internal.domain.enums.ApprovalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval_line")
@Comment("결재선")
public class ApprovalLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_line_id")
    @Comment("결재선 아이디")
    private Long approvalLineId;

    @Column(name = "approval_seq", columnDefinition = "TINYINT", nullable = false)
    @Comment("결재 순서")
    private byte approvalSeq;

    @Column(name = "approval_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Comment("진행 상태")
    private ApprovalStatus approvalStatus;

    @Column(name = "reason", length = 1000)
    @Comment("결재 의견")
    private String reason;

    @Column(name = "approved_dt")
    @Comment("처리 일시")
    private LocalDateTime approvedDate;

    @Column(name = "approval_id", nullable = false)
    @Comment("전자결재 아이디")
    private Long approvalId;

    @Column(name = "approver_id", nullable = false)
    @Comment("결재자 아이디")
    private Long approverId;

    @Column(name = "approver_name", length = 50, nullable = false)
    @Comment("결재자 이름")
    private String approverName;

    @Column(name = "approver_rank", nullable = false)
    @Comment("결재자 직급")
    private String approverRank;

    @Column(name = "read_dt")
    @Comment("조회 날짜")
    private LocalDateTime readDate;


}
