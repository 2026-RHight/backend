package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.ElectronicApprovalParam;
import com.reverse.approval.internal.persistence.row.ApprovalBoxRow;
import com.reverse.approval.internal.persistence.row.ApprovalDashboardCountsRow;
import com.reverse.approval.internal.persistence.row.ApprovalDashboardMyDraftRow;
import com.reverse.approval.internal.persistence.row.ApprovalDashboardPendingReviewRow;
import com.reverse.approval.internal.persistence.row.ApprovalHeaderRow;
import com.reverse.approval.internal.persistence.row.ApprovalMainItemRow;
import com.reverse.approval.internal.persistence.row.ApprovalProgressCountsRow;
import com.reverse.approval.internal.persistence.row.ApprovalProgressRow;
import com.reverse.approval.internal.persistence.row.ApprovalReviewRow;
import com.reverse.approval.internal.persistence.row.ApprovalVacationRow;
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

    int updateDocId(@Param("approvalId") Long approvalId, @Param("docId") String docId);

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

    Integer countApprovalReviews(@Param("employeeId") Long employeeId);

    List<ApprovalReviewRow> findApprovalReviews(
            @Param("employeeId") Long employeeId,
            @Param("offset") int offset,
            @Param("size") int size);

    ApprovalDashboardCountsRow findApprovalDashboardCounts(@Param("employeeId") Long employeeId);

    Integer countDashboardPendingReviews(@Param("employeeId") Long employeeId);

    Integer countDashboardInProgress(@Param("employeeId") Long employeeId);

    Integer countDashboardCompletedThisMonth(@Param("employeeId") Long employeeId);

    List<ApprovalDashboardPendingReviewRow> findApprovalDashboardPendingReviews(
            @Param("employeeId") Long employeeId, @Param("size") int size);

    List<ApprovalDashboardMyDraftRow> findApprovalDashboardMyDrafts(
            @Param("employeeId") Long employeeId, @Param("size") int size);

    Integer countMainPendingReviews(@Param("employeeId") Long employeeId);

    Integer countMainInProgressApprovals(@Param("employeeId") Long employeeId);

    List<ApprovalMainItemRow> findMainPendingReviews(
            @Param("employeeId") Long employeeId, @Param("size") int size);

    List<ApprovalMainItemRow> findMainInProgressApprovals(
            @Param("employeeId") Long employeeId, @Param("size") int size);

    Integer countAdminVacationApprovals(@Param("employeeIds") List<Long> employeeIds);

    List<ApprovalVacationRow> findAdminVacationApprovals(
            @Param("employeeIds") List<Long> employeeIds,
            @Param("offset") int offset,
            @Param("size") int size);

    Optional<ApprovalHeaderRow> findApprovalHeaderByApprovalId(
            @Param("approvalId") Long approvalId);
}
