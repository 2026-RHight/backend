package com.reverse.hr.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCareerRequestDTO(
        @NotBlank @Size(max = 255) String companyName,
        @NotBlank @Size(max = 255) String orgName,
        @NotNull LocalDate startDate,
        LocalDate endDate
) {}
