package com.reverse.hr.internal.persistence.param;

import com.reverse.hr.internal.domain.enums.SkillCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SkillCreateParam {
    private Long skillId;
    private Long employeeId;
    private SkillCategory category;
    private String skillName;
    private LocalDate acquisitionDate;
    private String licenseNumber;
    private Long hrFileId;
}
