package com.reverse.approval.internal.dto.request;

import com.reverse.approval.internal.domain.enums.DocType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;

@Getter
public class DraftApproval {
    @NotBlank(message = "제목이 없을 수는 없습니다.")
    String title;

    @NotNull(message = "신청서 양식이 없을 수는 없습니다.")
    DocType docType;

    VacationRequest vacationRequest;

    OvertimeRequest overtimeRequest;

    FlexibleWorkRequest flexibleWorkRequest;

    BusinessTripRequest businessTripRequest;

    LeaveRequest leaveRequest;

    RTWRequest rtwRequest;

    List<ApprovalLineRequest> approvalLine;

    List<ReferenceLineRequest> referenceLine;

    List<RecipientLineRequest> receipientLine;
}
