package com.reverse.performance.internal.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConfirmUpdateMapper {
    int updatePerformance(@Param("performanceId") Long performanceId, @Param("approvalId") Long approvalId);
}
