package com.reverse.approval.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "recipient_line")
@Comment("수신선 테이블")
public class ReceipientLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("수신선 아이디")
    private Long recipientId;

    @Column(name = "approval_id", nullable = false)
    @Comment("전자결재 아이디")
    private Long approvalId;

    @Column(name = "receiver_id", nullable = false)
    @Comment("수신자 아이디")
    private Long receiverId;

    @Column(name = "receiver_name", nullable = false, length = 50)
    @Comment("수신자 이름")
    private String receiverName;

    @Column(name = "receiver_rank", nullable = false)
    @Comment("수신자 직급")
    private String receiverRank;
}
