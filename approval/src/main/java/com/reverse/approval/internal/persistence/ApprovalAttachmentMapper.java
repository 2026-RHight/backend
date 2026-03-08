package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.ApprovalAttachmentParam;
import com.reverse.approval.internal.persistence.row.ApprovalAttachmentRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ApprovalAttachmentMapper {
    int insertApprovalAttachment(ApprovalAttachmentParam param);

    Optional<ApprovalAttachmentRow> findAttachmentByFileIdAndApprovalId(@Param("fileId") Long fileId,
                                                                         @Param("approvalId") Long approvalId);

    List<ApprovalAttachmentRow> findAttachmentsByApprovalId(@Param("approvalId") Long approvalId);
}
