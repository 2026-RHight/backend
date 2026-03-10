package com.reverse.performance.internal.persistence;

import com.reverse.performance.internal.dto.request.TeamEvalRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeamEvalMapper {
    void saveTeamEval(TeamEvalRequest dto);
}
