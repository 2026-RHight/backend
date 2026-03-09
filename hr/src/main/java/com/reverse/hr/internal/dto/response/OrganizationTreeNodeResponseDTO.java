package com.reverse.hr.internal.dto.response;

import com.reverse.hr.internal.domain.enums.OrgType;

public record OrganizationTreeNodeResponseDTO(
        Long orgId,
        Long parentOrgId,
        String orgName,
        OrgType orgType,
        Integer orgLevel,
        Integer sortOrder,
        Long memberCount) {}
