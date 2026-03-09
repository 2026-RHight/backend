package com.reverse.performance.internal.persistence;

import com.reverse.performance.internal.dto.request.EvalRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EvaluationMapper {
    void saveEvaluation(EvalRequest dto);
}
