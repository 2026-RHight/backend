package com.reverse.hr.internal.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SkillCategory {

    CERTIFICATE("자격"),
    LANGUAGE("어학"),
    LICENSE("면허"),
    ETC("기타");

    private final String description;
}
