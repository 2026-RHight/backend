package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.OvertimeDetailParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OvertimeDetailMapper {
    int insertOvertimeDetail(OvertimeDetailParam param);
}
