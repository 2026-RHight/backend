package com.reverse.performance.internal.application;

import com.reverse.performance.internal.dto.request.*;
import com.reverse.performance.internal.dto.response.*;
import com.reverse.performance.internal.domain.Status;
import com.reverse.performance.internal.domain.WorkItem;
import com.reverse.performance.internal.persistence.AttachmentMapper;
import com.reverse.performance.internal.persistence.CheckPerformanceMapper;
import com.reverse.performance.internal.persistence.ConfirmUpdateMapper;
import com.reverse.performance.internal.persistence.DashboardMapper;
import com.reverse.performance.internal.persistence.EvaluationMapper;
import com.reverse.performance.internal.persistence.InquiryMapper;
import com.reverse.performance.internal.persistence.MonthlyPerformanceMapper;
import com.reverse.performance.internal.persistence.PeerReviewMapper;
import com.reverse.performance.internal.persistence.PerformanceMapper;
import com.reverse.performance.internal.persistence.TeamEvalMapper;
import com.reverse.performance.internal.persistence.WaitingPerformanceMapper;
import com.reverse.performance.internal.exception.PerformanceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

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
    private final TeamEvalMapper teamEvalMapper;
    private final WaitingPerformanceMapper waitingPerformanceMapper;
    
    @Transactional
    public List<MyPerformanceResponse> findAllPerformance(Long id){
        return inquiryMapper.findInquiry(id);
    }

    @Transactional
    public List<EvaluatorPerformanceResponse> findAllEvaluatorPerformance(Long employeeId, Long appraiseeId, Status status) {
        return inquiryMapper.findEvaluatorPerformances(employeeId, appraiseeId, status);
    }

    @Transactional
    public void save(PerformanceCreateDTO dto) {
        if (dto == null || dto.request() == null || dto.request().workItem() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "request.workItem is required");
        }

        PerformanceRequest request = normalizePerformanceRequest(dto.request());
        if (request.difficultyScore() < 1 || request.difficultyScore() > 10) {
            throw new ResponseStatusException(BAD_REQUEST, "difficultyScore must be between 1 and 10");
        }
        performanceMapper.savePerformance(request);

        if (request.workItem() == WorkItem.PERSONAL) {
            if (dto.personalRequest() == null) {
                throw new ResponseStatusException(BAD_REQUEST, "personalRequest is required for PERSONAL");
            }
            performanceMapper.savePerformancePersonal(dto.personalRequest());
            return;
        }

        if (request.workItem() == WorkItem.TEAM) {
            if (dto.teamRequest() == null) {
                throw new ResponseStatusException(BAD_REQUEST, "teamRequest is required for TEAM");
            }
            performanceMapper.savePerformanceTeam(dto.teamRequest());
            return;
        }

        throw new ResponseStatusException(BAD_REQUEST, "Unsupported workItem");
    }
    @Transactional
    public List<PersonalPerformanceResponse> findAllPersonalPerformance(Long id, WorkItem workItem) {
        return checkPerformanceMapper.findPersonalPerformance(id, workItem);
    }
    @Transactional
    public List<TeamPerformanceResponse> findallTeamPerformance(Long id) {
        return checkPerformanceMapper.findTeamPerformance(id);
    }
    @Transactional
    public List<MonthlyResponse> findMonthlyPerformance(Long employeeId, Integer monthOffset) {
        YearMonth targetMonth = YearMonth.now().plusMonths(monthOffset == null ? 0L : monthOffset.longValue());
        return monthlyPerformanceMapper.findMonthlyPerformance(
                employeeId,
                targetMonth.getYear(),
                targetMonth.getMonthValue()
        );
    }
    @Transactional
    public void savePeerReview(PeerReviewRequest dto) {
        peerReviewMapper.savePeerReview(dto);
    }

    @Transactional
    public void saveAttachment(AttachmentRequest dto) {
        attachmentMapper.saveAttachment(dto);
    }
    @Transactional
    public void saveEvaluation(EvalRequest dto) {
        evaluationMapper.saveEvaluation(dto);
    }

    @Transactional
    public List<Dashboard> findDashboardInfo(Long id) {
        return dashboardMapper.findDashboard(id);
    }

    @Transactional
    public Long countEvaluatorPendingDashboard(Long employeeId) {
        Long count = dashboardMapper.countEvaluatorPendingDashboard(employeeId);
        return count == null ? 0L : count;
    }

    @Transactional
    public List<AppraiseePerformance> findWaitingPerformance(Long employeeId) {
        return waitingPerformanceMapper.findWaitingPerformance(employeeId);
    }

    public void saveTeamEval(TeamEvalRequest dto) {
        teamEvalMapper.saveTeamEval(dto);
    }

    public void updateConfirm(Long performanceId, Long approverId) {
        int updated = confirmUpdateMapper.updatePerformance(performanceId, approverId);
        if (updated == 0) {
            throw new PerformanceNotFoundException("확정할 성과를 찾을 수 없습니다.");
        }
    }

    @Transactional
    public void saveMonthlyScore(MonthlyScoreCreateRequest dto) {
        if (dto == null || dto.employeeId() == null || dto.year() == null || dto.month() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "employeeId, year, month are required");
        }
        MonthlyScoreCreateRequest normalized = normalizeMonthlyScoreRequest(dto);
        if (normalized.month() < 1 || normalized.month() > 12) {
            throw new ResponseStatusException(BAD_REQUEST, "month must be between 1 and 12");
        }

        Integer score = monthlyPerformanceMapper.calculateMonthlyScore(
                normalized.employeeId(),
                normalized.year(),
                normalized.month()
        );
        if (score == null) {
            throw new ResponseStatusException(BAD_REQUEST, "No achievement data to calculate monthly score");
        }

        monthlyPerformanceMapper.upsertMonthlyScore(
                normalized.employeeId(),
                normalized.year(),
                normalized.month(),
                score
        );
    }

    private PerformanceRequest normalizePerformanceRequest(PerformanceRequest request) {
        return new PerformanceRequest(
                request.performanceId(),
                request.employeeId(),
                request.title(),
                request.workItem(),
                request.startDate(),
                request.expectedEndDate(),
                request.workDetail(),
                request.status() == null ? Status.ACTIVE : request.status(),
                request.achievementRate() == null ? 0 : request.achievementRate(),
                request.difficultyScore() == null ? 5 : request.difficultyScore(),
                request.comment(),
                request.feedback()
        );
    }

    private MonthlyScoreCreateRequest normalizeMonthlyScoreRequest(MonthlyScoreCreateRequest dto) {
        Integer month = dto.month();
        if (month != null && month >= 0 && month <= 11) {
            month = month + 1;
        }
        return new MonthlyScoreCreateRequest(dto.employeeId(), dto.year(), month);
    }
}
