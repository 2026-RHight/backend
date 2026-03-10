package com.reverse.performance.internal.persistence;

import com.reverse.performance.internal.domain.WorkItem;
import com.reverse.performance.internal.dto.response.PersonalPerformanceResponse;
import com.reverse.performance.internal.dto.response.TeamPerformanceResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CheckPerformanceMapper {
    List<PersonalPerformanceResponse> findPersonalPerformance(
            @Param("id") Long id, @Param("workItem") WorkItem workItem);

    List<TeamPerformanceResponse> findTeamPerformance(@Param("id") Long id);
}
