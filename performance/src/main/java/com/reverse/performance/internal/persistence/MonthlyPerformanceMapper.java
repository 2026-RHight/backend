package com.reverse.performance.internal.persistence;

import com.reverse.performance.internal.dto.response.MonthlyResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MonthlyPerformanceMapper {
    List<MonthlyResponse> findMonthlyPerformance(
            @Param("employeeId") Long employeeId,
            @Param("targetYear") Integer targetYear,
            @Param("targetMonth") Integer targetMonth
    );

    Integer calculateMonthlyScore(
            @Param("employeeId") Long employeeId,
            @Param("targetYear") Integer targetYear,
            @Param("targetMonth") Integer targetMonth
    );

    void upsertMonthlyScore(
            @Param("employeeId") Long employeeId,
            @Param("targetYear") Integer targetYear,
            @Param("targetMonth") Integer targetMonth,
            @Param("score") Integer score
    );
}
