package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.FlexibleWorkParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FlexibleWorkDetailMapper {
    int insertFlexibleWorkDetail(FlexibleWorkParam param);
}
