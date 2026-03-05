package com.reverse.hr.internal.persistence.param;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBasicInfoParam {
    private Long employeeId;
    private String email;
    private String phone;
    private String address;
}
