package com.reverse.hr.internal.persistence.row;

import com.reverse.hr.internal.domain.enums.SkillCategory;

public record SkillItemRow(
        Long skillId,
        SkillCategory category,
        String skillName,
        String acquisitionDate,
        String licenseNumber,
        Long hrFileId
) {
}
