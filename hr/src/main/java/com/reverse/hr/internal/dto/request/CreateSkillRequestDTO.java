package com.reverse.hr.internal.dto.request;

import com.reverse.hr.internal.domain.enums.SkillCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateSkillRequestDTO (
        @NotNull SkillCategory category,
        @Size(max = 255)
        @NotBlank String skillName,
        @PastOrPresent
        @NotNull LocalDate acquisitionDate,
        String licenseNumber
) {
}
