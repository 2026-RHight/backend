package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.ReferenceLineParam;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReferenceLineMapper {
    int insertReferenceLine(ReferenceLineParam param);

    List<Long> findReferencerIdsByApprovalId(@Param("approvalId") Long approvalId);
}
