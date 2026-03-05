package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.LeaveDetailParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LeaveDetailMapper {
    int insertLeaveDetail(LeaveDetailParam param);
}
