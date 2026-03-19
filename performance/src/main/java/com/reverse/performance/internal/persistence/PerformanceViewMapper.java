package com.reverse.performance.internal.persistence;

import com.reverse.performance.internal.dto.response.PerformanceFeedbackResponse;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PerformanceViewMapper {
    Integer countPendingApprovalItems(@Param("employeeId") Long employeeId);

    List<PerformanceTrendPoint> findTrendPoints(@Param("employeeId") Long employeeId);

    List<PerformanceFeedbackResponse> findDashboardFeedbacks(@Param("employeeId") Long employeeId);

    List<InquiryItemRow> findInquiryItems(
            @Param("viewerEmployeeId") Long viewerEmployeeId,
            @Param("targetEmployeeId") Long targetEmployeeId,
            @Param("isAdmin") boolean isAdmin);

    List<InquiryItemRow> findInquiryItemsByEmployeeIds(
            @Param("employeeIds") List<Long> employeeIds);

    List<PerformanceMonthlyPoint> findMonthlyMyScores(
            @Param("viewerEmployeeId") Long viewerEmployeeId,
            @Param("targetEmployeeId") Long targetEmployeeId,
            @Param("isAdmin") boolean isAdmin,
            @Param("windowStart") LocalDate windowStart,
            @Param("windowEndExclusive") LocalDate windowEndExclusive);

    List<PerformanceMonthlyPoint> findMonthlyTeamScores(
            @Param("employeeIds") List<Long> employeeIds,
            @Param("windowStart") LocalDate windowStart,
            @Param("windowEndExclusive") LocalDate windowEndExclusive);

    List<PerformanceMonthlyDetailRow> findMonthlyDetailItems(
            @Param("viewerEmployeeId") Long viewerEmployeeId,
            @Param("targetEmployeeId") Long targetEmployeeId,
            @Param("isAdmin") boolean isAdmin,
            @Param("targetYear") Integer targetYear,
            @Param("targetMonth") Integer targetMonth);

    List<ApprovalItemRow> findApprovalItems(@Param("employeeId") Long employeeId);

    List<ApprovalItemRow> findApprovalItemsByMemberIds(@Param("memberIds") List<Long> memberIds);

    int updatePerformanceResult(
            @Param("performanceId") Long performanceId,
            @Param("progress") Integer progress,
            @Param("comment") String comment);

    int updatePersonalResult(
            @Param("performanceId") Long performanceId,
            @Param("resultSummary") String resultSummary,
            @Param("growthPoint") String growthPoint,
            @Param("improvementPoint") String improvementPoint);

    int updateTeamResult(
            @Param("performanceId") Long performanceId,
            @Param("resultSummary") String resultSummary,
            @Param("resultNote") String resultNote);

    int approvePerformance(
            @Param("employeeId") Long employeeId,
            @Param("performanceId") Long performanceId,
            @Param("comment") String comment);

    int rejectPerformance(
            @Param("employeeId") Long employeeId,
            @Param("performanceId") Long performanceId,
            @Param("comment") String comment);

    Long findPerformanceOwnerId(@Param("performanceId") Long performanceId);

    int countOwnedPerformance(
            @Param("employeeId") Long employeeId, @Param("performanceId") Long performanceId);

    Integer countInquiryAccessibleTarget(
            @Param("viewerEmployeeId") Long viewerEmployeeId,
            @Param("targetEmployeeId") Long targetEmployeeId);

    List<Long> findInquiryAccessibleTargetIds(@Param("viewerEmployeeId") Long viewerEmployeeId);

    List<TeamEvaluationMetricRow> findTeamEvaluationMetrics(
            @Param("evaluatorId") Long evaluatorId, @Param("employeeIds") List<Long> employeeIds);

    List<PeerReviewTargetStateRow> findPeerReviewTargetStates(
            @Param("employeeId") Long employeeId, @Param("employeeIds") List<Long> employeeIds);

    Long findPeerReviewableEvaluationId(@Param("appraiseeId") Long appraiseeId);

    List<TeamStatsMetricRow> findTeamStatsMetrics(@Param("employeeIds") List<Long> employeeIds);

    List<PerformanceTeamStatsTaskRow> findTeamStatsTasksByEmployeeIds(
            @Param("employeeIds") List<Long> employeeIds);

    record PerformanceTrendPoint(String monthLabel, Integer score) {}

    record PerformanceMonthlyPoint(Integer scoreYear, Integer scoreMonth, Integer score) {}

    record PerformanceMonthlyDetailRow(
            Long id,
            String type,
            String title,
            String date,
            Integer progress,
            Integer score,
            String description,
            String achievement,
            String feedbackText,
            String feedbackAuthor,
            String feedbackDate) {}

    record InquiryItemRow(
            Long id,
            String type,
            String title,
            String coreTask,
            String date,
            String status,
            Integer progress,
            Long employeeId,
            String description,
            String achievement) {}

    record ApprovalItemRow(
            Long id,
            Long targetEmployeeId,
            String title,
            String date,
            String status,
            String type,
            String achievement,
            Integer progress,
            String phase) {}

    record TeamEvaluationMetricRow(
            Long employeeId,
            String status,
            Integer systemScore,
            Double peerReviewScore,
            Double performanceAvg,
            Double attitudeAvg,
            Double collaborationAvg,
            Double creativityAvg) {}

    record PeerReviewTargetStateRow(Long employeeId, Boolean evaluated) {}

    record TeamStatsMetricRow(
            Long employeeId,
            Integer systemScore,
            Double performanceAvg,
            Double attitudeAvg,
            Double collaborationAvg,
            Double creativityAvg) {}

    record PerformanceTeamStatsTaskRow(
            Long employeeId, Long performanceId, String title, String status, String createdAt) {}
}
