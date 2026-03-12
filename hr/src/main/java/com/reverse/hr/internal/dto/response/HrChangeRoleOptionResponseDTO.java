package com.reverse.hr.internal.dto.response;

public record HrChangeRoleOptionResponseDTO(
        Long roleId, String roleCode, String roleName, String description) {}
