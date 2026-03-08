package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.LeaveDetailParam;
import com.reverse.approval.internal.persistence.row.LeaveDetailRow;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LeaveDetailMapper {
    int insertLeaveDetail(LeaveDetailParam param);

    Optional<LeaveDetailRow> findLeaveDetailByApprovalId(@Param("approvalId") Long approvalId);
}
