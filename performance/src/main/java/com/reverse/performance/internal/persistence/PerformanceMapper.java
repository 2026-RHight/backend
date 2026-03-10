package com.reverse.performance.internal.persistence;

import com.reverse.performance.internal.dto.request.PerformancePersonalRequest;
import com.reverse.performance.internal.dto.request.PerformanceRequest;
import com.reverse.performance.internal.dto.request.PerformanceTeamRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PerformanceMapper {
    void savePerformance(
            @Param("employeeId") Long employeeId, @Param("request") PerformanceRequest request);

    void savePerformancePersonal(PerformancePersonalRequest request);

    void savePerformanceTeam(PerformanceTeamRequest request);
}
