package com.reverse.performance.internal.application;

import com.reverse.performance.internal.dto.response.AdminEvalDataResponse;
import com.reverse.performance.internal.dto.response.AdminEvalMemberResponse;
import com.reverse.performance.internal.dto.response.AdminEvalTeamResponse;
import com.reverse.performance.internal.persistence.PerformanceViewMapper;
import java.time.Year;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceAdminEvalService {

    private final PerformanceViewMapper performanceViewMapper;
    private final PerformanceHrMemberResolver performanceHrMemberResolver;

    public List<AdminEvalTeamResponse> getAdminEvalTeams() {
        return performanceViewMapper.findAdminEvalTeams().stream()
                .map(r -> new AdminEvalTeamResponse(r.orgId(), r.name(), r.memberCount()))
                .toList();
    }

    public List<AdminEvalMemberResponse> getAdminEvalMembers(Long orgId) {
        return performanceViewMapper.findAdminEvalMembersByOrgId(orgId).stream()
                .map(
                        r -> {
                            Integer finalScore = r.finalScore();
                            boolean hasFinalScore = finalScore != null;
                            String finalGrade = hasFinalScore ? scoreToGrade(finalScore) : null;
                            return new AdminEvalMemberResponse(
                                    r.employeeId(),
                                    r.name(),
                                    r.position(),
                                    hasFinalScore,
                                    finalGrade);
                        })
                .toList();
    }

    public AdminEvalDataResponse getAdminEvalData(Long employeeId) {
        Long orgId = performanceHrMemberResolver.resolveOrgId(employeeId);
        PerformanceViewMapper.AdminEvalDataRow row =
                performanceViewMapper.findAdminEvalData(employeeId, orgId);
        if (row == null) {
            return new AdminEvalDataResponse(null, null, null, null, null);
        }
        Integer finalScore = row.finalScore();
        String finalGrade = finalScore != null ? scoreToGrade(finalScore) : null;
        return new AdminEvalDataResponse(
                row.teamEvalScore() != null ? row.teamEvalScore().intValue() : null,
                row.peerReviewScore(),
                row.systemScore(),
                finalScore,
                finalGrade);
    }

    @Transactional
    public void saveAdminFinalScore(Long employeeId, Long evaluatorId, String grade) {
        int score = gradeToScore(grade);
        int year = Year.now().getValue();
        performanceViewMapper.upsertAdminFinalScore(employeeId, evaluatorId, year, score);
    }

    private String scoreToGrade(int score) {
        if (score >= 90) return "S";
        if (score >= 80) return "A";
        if (score >= 70) return "B";
        if (score >= 60) return "C";
        return "D";
    }

    private int gradeToScore(String grade) {
        if (grade == null) return 50;
        return switch (grade.toUpperCase()) {
            case "S" -> 95;
            case "A" -> 85;
            case "B" -> 75;
            case "C" -> 65;
            default -> 50;
        };
    }
}
