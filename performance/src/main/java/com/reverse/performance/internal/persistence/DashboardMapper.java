package com.reverse.performance.internal.persistence;

import com.reverse.performance.internal.dto.response.Dashboard;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DashboardMapper {
    List<Dashboard> findDashboard(@Param("id") Long id);

    Long countEvaluatorPendingDashboard(@Param("employeeId") Long employeeId);
}
