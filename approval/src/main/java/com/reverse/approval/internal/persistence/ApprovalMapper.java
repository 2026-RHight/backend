package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.ElectronicApprovalParam;
import com.reverse.approval.internal.persistence.row.ApprovalBoxRow;
import com.reverse.approval.internal.persistence.row.ApprovalHeaderRow;
import com.reverse.approval.internal.persistence.row.ApprovalProgressCountsRow;
import com.reverse.approval.internal.persistence.row.ApprovalProgressRow;
import java.util.List;
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

    int updateReadDateIfNull(
            @Param("approvalId") Long approvalId, @Param("employeeId") Long employeeId);

    int countApprovalsByBox(@Param("employeeId") Long employeeId, @Param("boxType") String boxType);

    List<ApprovalBoxRow> findApprovalsByBox(
            @Param("employeeId") Long employeeId,
            @Param("boxType") String boxType,
            @Param("offset") int offset,
            @Param("size") int size);

    ApprovalProgressCountsRow findApprovalProgressCounts(@Param("employeeId") Long employeeId);

    int countApprovalProgress(
            @Param("employeeId") Long employeeId,
            @Param("tabType") String tabType,
            @Param("keyword") String keyword);

    List<ApprovalProgressRow> findApprovalProgress(
            @Param("employeeId") Long employeeId,
            @Param("tabType") String tabType,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("size") int size);

    Optional<ApprovalHeaderRow> findApprovalHeaderByApprovalId(
            @Param("approvalId") Long approvalId);
}
