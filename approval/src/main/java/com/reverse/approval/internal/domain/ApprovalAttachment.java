package com.reverse.approval.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "approval_attachment")
@Comment("전자결재 첨부파일")
public class ApprovalAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("첨부파일 아이디")
    private Long fileId;

    @Column(name = "file_path", length = 1024, nullable = false)
    @Comment("파일 경로")
    private String filePath;

    @Column(name = "file_key", length = 512)
    @Comment("S3 객체 키")
    private String fileKey;

    @Column(name = "original_name", length = 512, nullable = false)
    @Comment("원 파일 이름")
    private String originalName;

    @Column(name = "created_dt", nullable = false)
    @Comment("생성일")
    private LocalDateTime createDate;

    @Column(name = "approval_id", nullable = false)
    @Comment("전자결재 아이디")
    private Long approvalId;
}
