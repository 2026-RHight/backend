package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.RTWDetailParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RTWDetailMapper {
    int insertRtwDetail(RTWDetailParam param);
}
