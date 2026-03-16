package com.reverse.attendance.internal.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeeklyWorkScheduleProcessRequest {

    private Long approvalId;
    private boolean approve;

    @Size(max = 255)
    private String rejectReason;

    @AssertTrue(message = "반려 시 사유는 필수이고, 승인 시 사유는 비워야 합니다.")
    private boolean isRejectReasonStateValid() {
        return approve
                ? rejectReason == null || rejectReason.isBlank()
                : rejectReason != null && !rejectReason.isBlank();
    }
}
