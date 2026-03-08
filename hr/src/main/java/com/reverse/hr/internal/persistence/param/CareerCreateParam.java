package com.reverse.hr.internal.persistence.param;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
