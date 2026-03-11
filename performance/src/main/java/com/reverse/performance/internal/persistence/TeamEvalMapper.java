package com.reverse.performance.internal.persistence;

import com.reverse.performance.internal.dto.request.TeamEvalRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TeamEvalMapper {
    void saveTeamEval(TeamEvalRequest dto);

    int countByEvaluatorIdAndAppraiseeIdAndYear(
            @Param("evaluatorId") Long evaluatorId,
            @Param("appraiseeId") Long appraiseeId,
            @Param("evaluationYear") Integer evaluationYear);
}
