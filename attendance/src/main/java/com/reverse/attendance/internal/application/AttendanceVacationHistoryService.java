package com.reverse.attendance.internal.application;

import com.reverse.attendance.internal.dto.response.AttendanceVacationHistoryItemResponse;
import com.reverse.attendance.internal.persistence.ApprovalVacationHistoryMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceVacationHistoryService {

    private final ApprovalVacationHistoryMapper approvalVacationHistoryMapper;

    @Transactional(readOnly = true)
    public List<AttendanceVacationHistoryItemResponse> getMyVacationHistory(Long employeeId) {
        return approvalVacationHistoryMapper.findMyVacationHistory(employeeId);
    }
}
