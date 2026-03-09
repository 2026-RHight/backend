package com.reverse.performance.internal.persistence;

import com.reverse.performance.internal.dto.response.PerformanceApprovalItemResponse;
import com.reverse.performance.internal.dto.response.PerformanceFeedbackResponse;
import com.reverse.performance.internal.dto.response.PerformanceInquiryItemResponse;
import com.reverse.performance.internal.dto.response.PerformancePeerReviewTargetResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PerformanceViewMapper {
    Integer countPendingApprovalItems(@Param("employeeId") Long employeeId);

    List<PerformanceTrendPoint> findTrendPoints(@Param("employeeId") Long employeeId);

    List<PerformanceFeedbackResponse> findDashboardFeedbacks(@Param("employeeId") Long employeeId);

    List<PerformanceInquiryItemResponse> findInquiryItems(
            @Param("viewerEmployeeId") Long viewerEmployeeId,
            @Param("targetEmployeeId") Long targetEmployeeId,
            @Param("isAdmin") boolean isAdmin
    );

    List<PerformanceMonthlyPoint> findMonthlyMyScores(
            @Param("viewerEmployeeId") Long viewerEmployeeId,
            @Param("targetEmployeeId") Long targetEmployeeId,
            @Param("isAdmin") boolean isAdmin
    );

    List<PerformanceMonthlyPoint> findMonthlyTeamScores(
            @Param("viewerEmployeeId") Long viewerEmployeeId,
            @Param("targetEmployeeId") Long targetEmployeeId,
            @Param("isAdmin") boolean isAdmin
    );

    List<PerformanceMonthlyDetailRow> findMonthlyDetailItems(
            @Param("viewerEmployeeId") Long viewerEmployeeId,
            @Param("targetEmployeeId") Long targetEmployeeId,
            @Param("isAdmin") boolean isAdmin,
            @Param("targetYear") Integer targetYear,
            @Param("targetMonth") Integer targetMonth
    );

    List<PerformanceApprovalItemResponse> findApprovalItems(@Param("employeeId") Long employeeId);

    int updatePerformanceResult(
            @Param("performanceId") Long performanceId,
            @Param("progress") Integer progress,
            @Param("comment") String comment
    );

    int updatePersonalResult(
            @Param("performanceId") Long performanceId,
            @Param("resultSummary") String resultSummary,
            @Param("growthPoint") String growthPoint,
            @Param("improvementPoint") String improvementPoint
    );

    int updateTeamResult(
            @Param("performanceId") Long performanceId,
            @Param("resultSummary") String resultSummary,
            @Param("resultNote") String resultNote
    );

    int approvePerformance(
            @Param("employeeId") Long employeeId,
            @Param("performanceId") Long performanceId,
            @Param("comment") String comment
    );

    int rejectPerformance(
            @Param("employeeId") Long employeeId,
            @Param("performanceId") Long performanceId,
            @Param("comment") String comment
    );

    List<PerformanceTeamEvaluationTargetRow> findTeamEvaluationTargets(@Param("employeeId") Long employeeId);

    List<PerformancePeerReviewTargetResponse> findPeerReviewTargets(@Param("employeeId") Long employeeId);

    Long findLatestEvaluationId(@Param("appraiseeId") Long appraiseeId);

    List<String> findManagedTeams(@Param("employeeId") Long employeeId);

    List<PerformanceTeamStatsMemberRow> findTeamStatsMembers(
            @Param("employeeId") Long employeeId,
            @Param("teamName") String teamName
    );

    List<PerformanceTeamStatsTaskRow> findTeamStatsTasks(
            @Param("employeeId") Long employeeId,
            @Param("teamName") String teamName
    );

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
            String feedbackDate
    ) {}

    record PerformanceTeamEvaluationTargetRow(
            Long id,
            String name,
            String role,
            String department,
            String status,
            Integer systemScore,
            Double peerReviewScore,
            Double performanceAvg,
            Double attitudeAvg,
            Double collaborationAvg,
            Double creativityAvg
    ) {}

    record PerformanceTeamStatsMemberRow(
            Long id,
            String name,
            String role,
            String department,
            Integer systemScore,
            Double performanceAvg,
            Double attitudeAvg,
            Double collaborationAvg,
            Double creativityAvg
    ) {}

    record PerformanceTeamStatsTaskRow(
            Long employeeId,
            Long performanceId,
            String title,
            String status,
            String createdAt
    ) {}
}
