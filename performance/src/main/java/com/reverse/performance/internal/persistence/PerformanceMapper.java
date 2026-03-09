package com.reverse.performance.internal.persistence;

import com.reverse.performance.internal.dto.request.PerformancePersonalRequest;
import com.reverse.performance.internal.dto.request.PerformanceRequest;
import com.reverse.performance.internal.dto.request.PerformanceTeamRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PerformanceMapper {
    void savePerformance(PerformanceRequest request);

    void savePerformancePersonal(PerformancePersonalRequest request);

    void savePerformanceTeam(PerformanceTeamRequest request);
}
