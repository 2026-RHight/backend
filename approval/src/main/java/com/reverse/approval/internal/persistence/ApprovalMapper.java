package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.ElectronicApprovalParam;
import com.reverse.approval.internal.persistence.row.ApprovalHeaderRow;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApprovalMapper {
    int insertElectronicApproval(ElectronicApprovalParam param);

    int countByApprovalId(@Param("approvalId") Long approvalId);

    int countByApprovalIdAndDrafterId(
            @Param("approvalId") Long approvalId, @Param("drafterId") Long drafterId);

    int deleteElectronicApprovalById(@Param("approvalId") Long approvalId);

    int updateApprovalStatusFromTempToPending(
            @Param("approvalId") Long approvalId, @Param("drafterId") Long drafterId);

    String findTitleByApprovalId(@Param("approvalId") Long approvalId);

    String findApprovalStatusByApprovalId(@Param("approvalId") Long approvalId);

    Long findDrafterIdByApprovalId(@Param("approvalId") Long approvalId);

    int updateApprovalToComplete(@Param("approvalId") Long approvalId);

    int updateApprovalToRejected(@Param("approvalId") Long approvalId);

    int updateApprovalToHold(@Param("approvalId") Long approvalId);

    Optional<ApprovalHeaderRow> findApprovalHeaderByApprovalId(
            @Param("approvalId") Long approvalId);
}
