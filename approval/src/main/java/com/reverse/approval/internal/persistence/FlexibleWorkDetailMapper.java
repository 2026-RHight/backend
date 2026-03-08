package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.FlexibleWorkParam;
import com.reverse.approval.internal.persistence.row.FlexibleWorkDetailRow;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FlexibleWorkDetailMapper {
    int insertFlexibleWorkDetail(FlexibleWorkParam param);

    Optional<FlexibleWorkDetailRow> findFlexibleWorkDetailByApprovalId(
            @Param("approvalId") Long approvalId);
}
