package com.reverse.performance.internal.persistence;

import com.reverse.performance.internal.dto.response.EvaluatorPerformanceResponse;
import com.reverse.performance.internal.dto.response.MyPerformanceResponse;
import com.reverse.performance.internal.domain.Status;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InquiryMapper {
    List<MyPerformanceResponse> findInquiry(@Param("id") Long id);

    List<EvaluatorPerformanceResponse> findEvaluatorPerformances(
            @Param("employeeId") Long employeeId,
            @Param("appraiseeId") Long appraiseeId,
            @Param("status") Status status
    );
}
