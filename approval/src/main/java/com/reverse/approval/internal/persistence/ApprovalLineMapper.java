package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.ApprovalLineParam;
import com.reverse.approval.internal.persistence.row.ApprovalLineDetailRow;
import com.reverse.approval.internal.persistence.row.ApprovalLineRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApprovalLineMapper {
    int insertApprovalLine(ApprovalLineParam param);

    int updateApprovalLineStatusFromTempToPending(@Param("approvalId") Long approvalId);

    Long findFirstApproverIdByApprovalId(@Param("approvalId") Long approvalId);

    ApprovalLineRow findFirstPendingLineByApprovalId(@Param("approvalId") Long approvalId);

    int updateApprovalLineToComplete(
            @Param("approvalLineId") Long approvalLineId, @Param("reason") String reason);

    int updateApprovalLineToRejected(
            @Param("approvalLineId") Long approvalLineId, @Param("reason") String reason);

    int updateApprovalLineToHold(
            @Param("approvalLineId") Long approvalLineId, @Param("reason") String reason);

    int countPendingLinesByApprovalId(@Param("approvalId") Long approvalId);

    List<Long> findPendingApproverIdsByApprovalId(@Param("approvalId") Long approvalId);

    List<ApprovalLineDetailRow> findLinesByApprovalId(@Param("approvalId") Long approvalId);

    int countByApprovalIdAndApproverId(
            @Param("approvalId") Long approvalId, @Param("approverId") Long approverId);
}
