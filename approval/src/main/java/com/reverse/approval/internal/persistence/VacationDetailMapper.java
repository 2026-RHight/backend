package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.VacationDetailParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VacationDetailMapper {
    int insertVacationDetail(VacationDetailParam param);
}
