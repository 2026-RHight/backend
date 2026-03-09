package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.ReferenceLineParam;
import com.reverse.approval.internal.persistence.row.ReferenceLineDetailRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReferenceLineMapper {
    int insertReferenceLine(ReferenceLineParam param);

    List<Long> findReferencerIdsByApprovalId(@Param("approvalId") Long approvalId);

    List<ReferenceLineDetailRow> findReferenceLinesByApprovalId(
            @Param("approvalId") Long approvalId);

    int countByApprovalIdAndReferencerId(
            @Param("approvalId") Long approvalId, @Param("referencerId") Long referencerId);

    int updateReadDateIfNull(
            @Param("approvalId") Long approvalId, @Param("referencerId") Long referencerId);
}
