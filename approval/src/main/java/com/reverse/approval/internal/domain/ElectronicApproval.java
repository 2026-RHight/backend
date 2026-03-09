package com.reverse.approval.internal.domain;

import com.reverse.approval.internal.domain.enums.ApprovalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import org.hibernate.annotations.Comment;

@Entity
@Table(
        name = "electronic_approval",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_electronic_approval_doc_id",
                        columnNames = {"doc_id"}))
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "doc_type")
@Comment("전자 결재 테이블")
public abstract class ElectronicApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_id")
    @Comment("전자결재 아이디")
    private Long approvalId;

    @Column(name = "doc_id", length = 13)
    @Comment("문서 번호")
    private String docId;

    @Column(name = "title", length = 1000, nullable = false)
    @Comment("제목")
    private String title;

    @Column(name = "approval_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Comment("진행 상태")
    private ApprovalStatus approvalStatus;

    @Column(name = "draft_dt", nullable = false)
    @Comment("기안일")
    private LocalDateTime draftDate;

    @Column(name = "drafter_id", nullable = false)
    @Comment("기안자 아이디")
    private Long drafterId;

    @Column(name = "drafter_name", nullable = false)
    @Comment("기안자 이름")
    private String drafterName;

    @Column(name = "department_name", nullable = false)
    @Comment("기안자 소속")
    private String departmentName;

    @Column(name = "approve_dt")
    @Comment("승인일")
    private LocalDateTime approveDate;

    @Column(name = "read_dt")
    @Comment("조회 날짜")
    private LocalDateTime readDate;
}
