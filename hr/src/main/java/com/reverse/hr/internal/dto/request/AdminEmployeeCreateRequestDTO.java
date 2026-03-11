package com.reverse.hr.internal.dto.request;

import com.reverse.hr.internal.domain.enums.EmployType;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import com.reverse.hr.internal.domain.enums.RecruitType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record AdminEmployeeCreateRequestDTO(
        @NotBlank(message = "이름은 필수입니다.") @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
                String employeeName,
        @NotBlank(message = "이메일은 필수입니다.")
                @Email(message = "이메일 형식이 올바르지 않습니다.")
                @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
                String email,
        @NotBlank(message = "연락처는 필수입니다.") @Size(max = 50, message = "연락처는 50자 이하여야 합니다.")
                String phone,
        @NotBlank(message = "내선번호는 필수입니다.") @Size(max = 50, message = "내선번호는 50자 이하여야 합니다.")
                String extensionNum,
        @NotNull(message = "생년월일은 필수입니다.") LocalDate birthDate,
        @NotBlank(message = "주소는 필수입니다.") @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
                String address,
        @NotBlank(message = "주민등록번호는 필수입니다.")
                @Pattern(regexp = "^[0-9]{13}$", message = "주민등록번호는 하이픈 없이 13자리 숫자여야 합니다.")
                String residentNumber,
        @NotBlank(message = "은행명은 필수입니다.") @Size(max = 50, message = "은행명은 50자 이하여야 합니다.")
                String bankName,
        @NotBlank(message = "계좌번호는 필수입니다.")
                @Pattern(regexp = "^[0-9]{8,30}$", message = "계좌번호는 하이픈 없이 8~30자리 숫자여야 합니다.")
                String accountNumber,
        @NotNull(message = "조직은 필수입니다.") @Min(value = 1, message = "조직 ID는 1 이상이어야 합니다.")
                Long orgId,
        @NotNull(message = "직무는 필수입니다.") @Min(value = 1, message = "직무 ID는 1 이상이어야 합니다.")
                Long jobId,
        @NotNull(message = "직책은 필수입니다.") @Min(value = 1, message = "직책 ID는 1 이상이어야 합니다.")
                Long positionId,
        @NotNull(message = "직급은 필수입니다.") @Min(value = 1, message = "직급 ID는 1 이상이어야 합니다.")
                Long rankId,
        @NotNull(message = "재직 상태는 필수입니다.") EmployeeState employeeState,
        @NotNull(message = "고용 형태는 필수입니다.") EmployType employType,
        @NotNull(message = "입사 유형은 필수입니다.") RecruitType recruitType,
        @NotNull(message = "근무지는 필수입니다.") @Min(value = 1, message = "근무지 ID는 1 이상이어야 합니다.")
                Long areaId,
        @NotNull(message = "입사일은 필수입니다.") LocalDate hireDate,
        @NotNull(message = "권한 목록은 필수입니다.")
                List<
                                @NotNull(message = "권한 ID는 null일 수 없습니다.")
                                @Min(value = 1, message = "권한 ID는 1 이상이어야 합니다.") Long>
                        roleIds) {

    @AssertTrue(message = "생년월일은 오늘 이후일 수 없습니다.")
    public boolean isBirthDateValid() {
        if (birthDate == null) {
            return true;
        }
        return !birthDate.isAfter(LocalDate.now());
    }

    @AssertTrue(message = "입사일은 생년월일보다 이전일 수 없습니다.")
    public boolean isHireDateNotBeforeBirthDate() {
        if (birthDate == null || hireDate == null) {
            return true;
        }
        return !hireDate.isBefore(birthDate);
    }
}
