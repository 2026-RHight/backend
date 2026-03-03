package com.reverse.approval.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "reference_line")
@Comment("참조선 테이블")
public class ReferenceLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id")
    @Comment("참조선 아이디")
    private Long referenceId;

    @Column(name = "approval_id", nullable = false)
    @Comment("전자결재 아이디")
    private Long approvalId;

    @Column(name = "referencer_id", nullable = false)
    @Comment("참조자 아이디")
    private Long referencerId;

    @Column(name = "referencer_name", nullable = false, length = 50)
    @Comment("참조자 이름")
    private String referencerName;

    @Column(name = "reference_rank", nullable = false)
    @Comment("참조자 직급")
    private String referenceRank;

}
