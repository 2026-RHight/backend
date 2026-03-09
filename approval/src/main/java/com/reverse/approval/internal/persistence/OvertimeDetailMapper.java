package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.OvertimeDetailParam;
import com.reverse.approval.internal.persistence.row.OvertimeDetailRow;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OvertimeDetailMapper {
    int insertOvertimeDetail(OvertimeDetailParam param);

    Optional<OvertimeDetailRow> findOvertimeDetailByApprovalId(
            @Param("approvalId") Long approvalId);
}
