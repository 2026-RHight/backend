package com.reverse.hr.internal.persistence.row;

public record HrChangeRoleOptionRow(
        Long roleId, String roleCode, String roleName, String description) {}
