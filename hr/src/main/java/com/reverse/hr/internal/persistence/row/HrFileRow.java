package com.reverse.hr.internal.persistence.row;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrFileRow {

    private Long hrFileId;
    private String fileUrl;
    private String fileTitle;
}
