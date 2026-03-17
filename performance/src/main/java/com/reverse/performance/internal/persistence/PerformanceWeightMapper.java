package com.reverse.performance.internal.persistence;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PerformanceWeightMapper {
    List<PerformanceWeightRow> findAllTeamWeights();

    Integer countTeamOrganizations(@Param("orgIds") List<Long> orgIds);

    int updateWeight(
            @Param("orgId") Long orgId,
            @Param("personalWeightRate") Integer personalWeightRate,
            @Param("teamWeightRate") Integer teamWeightRate);

    int insertWeight(
            @Param("orgId") Long orgId,
            @Param("personalWeightRate") Integer personalWeightRate,
            @Param("teamWeightRate") Integer teamWeightRate);

    List<Long> findEmployeeIdsByOrgIds(@Param("orgIds") List<Long> orgIds);

    LocalDateTime findLatestUpdatedAtByOrgId(@Param("orgId") Long orgId);

    record PerformanceWeightRow(
            Long orgId,
            Integer personalWeightRate,
            Integer teamWeightRate,
            LocalDateTime updatedAt) {}
}
