package com.reverse.attendance.internal.application;

import com.reverse.attendance.internal.dto.response.AttendanceRequestHistoryItemResponse;
import com.reverse.attendance.internal.dto.response.RequestStatusCountResponse;
import com.reverse.attendance.internal.persistence.ApprovalRequestHistoryMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceRequestHistoryService {

    private final ApprovalRequestHistoryMapper approvalRequestHistoryMapper;

    @Transactional(readOnly = true)
    public List<AttendanceRequestHistoryItemResponse> getMyRequestHistory(Long employeeId) {
        return approvalRequestHistoryMapper.findMyRequestHistory(employeeId);
    }

    @Transactional(readOnly = true)
    public RequestStatusCountResponse getMyRequestHistoryCounts(Long employeeId) {
        RequestStatusCountResponse response =
                approvalRequestHistoryMapper.countMyRequestHistory(employeeId);
        if (response == null) {
            return RequestStatusCountResponse.builder()
                    .pendingCount(0)
                    .approvedCount(0)
                    .rejectedCount(0)
                    .build();
        }
        return response;
    }
}
