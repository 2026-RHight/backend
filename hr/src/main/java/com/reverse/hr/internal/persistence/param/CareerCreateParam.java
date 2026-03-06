package com.reverse.hr.internal.persistence.param;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CareerCreateParam {
    private Long careerId;
    private Long employeeId;
    private String companyName;
    private String orgName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long hrFileId;
}

