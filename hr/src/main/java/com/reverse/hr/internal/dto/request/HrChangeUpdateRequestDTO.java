package com.reverse.hr.internal.dto.request;

import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record HrChangeUpdateRequestDTO(
        @Min(value = 1, message = "조직 ID는 1 이상이어야 합니다.") Long orgId,
        @Min(value = 1, message = "직무 ID는 1 이상이어야 합니다.") Long jobId,
        @Min(value = 1, message = "직책 ID는 1 이상이어야 합니다.") Long positionId,
        @Min(value = 1, message = "직급 ID는 1 이상이어야 합니다.") Long rankId,
        EmployeeState employeeState,
        EmployType employType,
        @Min(value = 1, message = "근무지 ID는 1 이상이어야 합니다.") Long areaId,
        LocalDate effectiveFrom,
        @Size(max = 255, message = "변경 사유는 255자 이하여야 합니다.") String reason,
        List<
                        @NotNull(message = "권한 ID는 null일 수 없습니다.")
                        @Min(value = 1, message = "권한 ID는 1 이상이어야 합니다.") Long>
                roleIds) {}
