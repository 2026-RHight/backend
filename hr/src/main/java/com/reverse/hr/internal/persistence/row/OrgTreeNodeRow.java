package com.reverse.hr.internal.persistence.row;

import com.reverse.hr.internal.domain.enums.OrgType;

public record OrgTreeNodeRow(
        Long orgId,
        Long parentOrgId,
        String orgName,
        OrgType orgType,
        Integer orgLevel,
        Integer sortOrder,
        Long memberCount) {}
