package com.reverse.performance.internal.application;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.reverse.core.exception.ForbiddenException;
import com.reverse.performance.internal.domain.Status;
import com.reverse.performance.internal.domain.WorkItem;
import com.reverse.performance.internal.dto.request.*;
import com.reverse.performance.internal.dto.response.*;
import com.reverse.performance.internal.exception.PerformanceActionNotAllowedException;
import com.reverse.performance.internal.exception.PerformanceNotFoundException;
import com.reverse.performance.internal.persistence.AttachmentMapper;
import com.reverse.performance.internal.persistence.CheckPerformanceMapper;
import com.reverse.performance.internal.persistence.ConfirmUpdateMapper;
import com.reverse.performance.internal.persistence.DashboardMapper;
import com.reverse.performance.internal.persistence.EvaluationMapper;
import com.reverse.performance.internal.persistence.InquiryMapper;
import com.reverse.performance.internal.persistence.MonthlyPerformanceMapper;
import com.reverse.performance.internal.persistence.PeerReviewMapper;
import com.reverse.performance.internal.persistence.PerformanceMapper;
import com.reverse.performance.internal.persistence.PerformanceViewMapper;
import com.reverse.performance.internal.persistence.TeamEvalMapper;
import com.reverse.performance.internal.persistence.WaitingPerformanceMapper;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final AttachmentMapper attachmentMapper;
    private final CheckPerformanceMapper checkPerformanceMapper;
    private final ConfirmUpdateMapper confirmUpdateMapper;
    private final DashboardMapper dashboardMapper;
    private final EvaluationMapper evaluationMapper;
    private final InquiryMapper inquiryMapper;
    private final MonthlyPerformanceMapper monthlyPerformanceMapper;
    private final PeerReviewMapper peerReviewMapper;
    private final PerformanceMapper performanceMapper;
    private final PerformanceViewMapper performanceViewMapper;
    private final TeamEvalMapper teamEvalMapper;
    private final WaitingPerformanceMapper waitingPerformanceMapper;

    @Transactional(readOnly = true)
    public List<MyPerformanceResponse> findAllPerformance(Long id) {
        return inquiryMapper.findInquiry(id);
    }

    @Transactional(readOnly = true)
    public List<EvaluatorPerformanceResponse> findAllEvaluatorPerformance(
            Long employeeId, Long appraiseeId, Status status) {
        return inquiryMapper.findEvaluatorPerformances(employeeId, appraiseeId, status);
    }

    @Transactional
    public void save(Long employeeId, PerformanceCreateDTO dto) {
        if (dto == null || dto.request() == null || dto.request().getWorkItem() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "request.workItem is required");
        }

        PerformanceRequest request = normalizePerformanceRequest(dto.request());
        if (request.getDifficultyScore() < 1 || request.getDifficultyScore() > 10) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "difficultyScore must be between 1 and 10");
        }
        performanceMapper.savePerformance(employeeId, request);
        Long performanceId = request.getPerformanceId();
        if (performanceId == null) {
            throw new IllegalStateException("성과 ID 생성에 실패했습니다.");
        }

        if (request.getWorkItem() == WorkItem.PERSONAL) {
            if (dto.personalRequest() == null) {
                throw new ResponseStatusException(
                        BAD_REQUEST, "personalRequest is required for PERSONAL");
            }
            performanceMapper.savePerformancePersonal(
                    dto.personalRequest().withPerformanceId(performanceId));
            return;
        }

        if (request.getWorkItem() == WorkItem.TEAM) {
            if (dto.teamRequest() == null) {
                throw new ResponseStatusException(BAD_REQUEST, "teamRequest is required for TEAM");
            }
            performanceMapper.savePerformanceTeam(
                    dto.teamRequest().withPerformanceId(performanceId));
            return;
        }

        throw new ResponseStatusException(BAD_REQUEST, "Unsupported workItem");
    }

    @Transactional(readOnly = true)
    public List<PersonalPerformanceResponse> findAllPersonalPerformance(
            Long id, WorkItem workItem) {
        return checkPerformanceMapper.findPersonalPerformance(id, workItem);
    }

    @Transactional(readOnly = true)
    public List<TeamPerformanceResponse> findallTeamPerformance(Long id) {
        return checkPerformanceMapper.findTeamPerformance(id);
    }

    @Transactional(readOnly = true)
    public List<MonthlyResponse> findMonthlyPerformance(Long employeeId, Integer monthOffset) {
        YearMonth targetMonth =
                YearMonth.now().plusMonths(monthOffset == null ? 0L : monthOffset.longValue());
        return monthlyPerformanceMapper.findMonthlyPerformance(
                employeeId, targetMonth.getYear(), targetMonth.getMonthValue());
    }

    @Transactional
    public void savePeerReview(PeerReviewRequest dto) {
        if (dto == null || dto.evalId() == null || dto.reviewerId() == null) {
            throw new PerformanceActionNotAllowedException("동료 평가 대상 정보가 올바르지 않습니다.");
        }
        if (peerReviewMapper.countByEvalIdAndReviewerId(dto.evalId(), dto.reviewerId()) > 0) {
            throw new PerformanceActionNotAllowedException("이미 동료 평가를 등록했습니다.");
        }
        peerReviewMapper.savePeerReview(dto);
    }

    @Transactional
    public void saveAttachment(AttachmentRequest dto, Long employeeId) {
        if (dto == null || dto.performanceId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "performanceId is required");
        }
        validateOwnedPerformance(employeeId, dto.performanceId());
        attachmentMapper.saveAttachment(dto);
    }

    @Transactional
    public void saveEvaluation(EvalRequest dto) {
        evaluationMapper.saveEvaluation(dto);
    }

    @Transactional(readOnly = true)
    public List<Dashboard> findDashboardInfo(Long id) {
        return dashboardMapper.findDashboard(id);
    }

    @Transactional(readOnly = true)
    public Long countEvaluatorPendingDashboard(Long employeeId) {
        Long count = dashboardMapper.countEvaluatorPendingDashboard(employeeId);
        return count == null ? 0L : count;
    }

    @Transactional(readOnly = true)
    public List<AppraiseePerformance> findWaitingPerformance(Long employeeId) {
        return waitingPerformanceMapper.findWaitingPerformance(employeeId);
    }

    @Transactional
    public void saveTeamEval(TeamEvalRequest dto) {
        if (dto == null) {
            throw new ResponseStatusException(BAD_REQUEST, "team evaluation request is required");
        }
        TeamEvalRequest normalized =
                dto.withEvaluationYear(resolveEvaluationYear(dto.evaluationYear()));
        teamEvalMapper.saveTeamEval(normalized);
    }

    @Transactional
    public void updateConfirm(Long performanceId, Long evaluatorId) {
        int updated = confirmUpdateMapper.updatePerformance(performanceId, evaluatorId);
        if (updated == 0) {
            throw new PerformanceNotFoundException("확정할 성과를 찾을 수 없습니다.");
        }
    }

    @Transactional
    public void saveMonthlyScore(Long employeeId, MonthlyScoreCreateRequest dto) {
        if (dto == null || employeeId == null || dto.year() == null || dto.month() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "employeeId, year, month are required");
        }
        MonthlyScoreCreateRequest normalized = normalizeMonthlyScoreRequest(dto);

        Integer score =
                monthlyPerformanceMapper.calculateMonthlyScore(
                        employeeId, normalized.year(), normalized.month());
        if (score == null) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "No achievement data to calculate monthly score");
        }

        monthlyPerformanceMapper.upsertMonthlyScore(
                employeeId, normalized.year(), normalized.month(), score);
    }

    private PerformanceRequest normalizePerformanceRequest(PerformanceRequest request) {
        return new PerformanceRequest(
                request.getPerformanceId(),
                request.getTitle(),
                request.getWorkItem(),
                request.getStartDate(),
                request.getExpectedEndDate(),
                request.getWorkDetail(),
                request.getStatus() == null ? Status.ACTIVE : request.getStatus(),
                request.getAchievementRate() == null ? 0 : request.getAchievementRate(),
                request.getDifficultyScore() == null ? 5 : request.getDifficultyScore(),
                request.getComment(),
                request.getFeedback());
    }

    private MonthlyScoreCreateRequest normalizeMonthlyScoreRequest(MonthlyScoreCreateRequest dto) {
        Integer month = dto.month();
        if (month != null && (month < 1 || month > 12)) {
            throw new ResponseStatusException(BAD_REQUEST, "month must be between 1 and 12");
        }
        return new MonthlyScoreCreateRequest(dto.year(), month);
    }

    private Integer resolveEvaluationYear(Integer evaluationYear) {
        return evaluationYear == null ? Year.now().getValue() : evaluationYear;
    }

    private void validateOwnedPerformance(Long employeeId, Long performanceId) {
        if (employeeId == null) {
            throw new ForbiddenException("FORBIDDEN", "성과 첨부를 등록할 권한이 없습니다.");
        }
        int authorized = performanceViewMapper.countOwnedPerformance(employeeId, performanceId);
        if (authorized == 0) {
            throw new ForbiddenException("FORBIDDEN", "성과 첨부를 등록할 권한이 없습니다.");
        }
    }
}
