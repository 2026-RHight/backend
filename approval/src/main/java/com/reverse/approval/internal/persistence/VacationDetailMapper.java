package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.VacationDetailParam;
import com.reverse.approval.internal.persistence.row.VacationDetailRow;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VacationDetailMapper {
    int insertVacationDetail(VacationDetailParam param);

    Optional<VacationDetailRow> findVacationDetailByApprovalId(
            @Param("approvalId") Long approvalId);
}
