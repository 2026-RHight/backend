package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.RTWDetailParam;
import com.reverse.approval.internal.persistence.row.RTWDetailRow;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RTWDetailMapper {
    int insertRtwDetail(RTWDetailParam param);

    Optional<RTWDetailRow> findRTWDetailByApprovalId(@Param("approvalId") Long approvalId);
}
