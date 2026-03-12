package com.reverse.hr.internal.dto.response;

import java.util.List;

public record HrChangeOptionsResponseDTO(
        List<HrChangeSimpleOptionResponseDTO> organizations,
        List<HrChangeSimpleOptionResponseDTO> jobs,
        List<HrChangeSimpleOptionResponseDTO> positions,
        List<HrChangeSimpleOptionResponseDTO> ranks,
        List<HrChangeSimpleOptionResponseDTO> workingAreas,
        List<HrChangeRoleOptionResponseDTO> roles) {}
