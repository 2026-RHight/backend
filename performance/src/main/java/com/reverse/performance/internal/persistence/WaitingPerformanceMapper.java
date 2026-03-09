package com.reverse.performance.internal.persistence;

import com.reverse.performance.internal.dto.response.AppraiseePerformance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WaitingPerformanceMapper {
    List<AppraiseePerformance> findWaitingPerformance(@Param("employeeId") Long evaluatorId);
}
