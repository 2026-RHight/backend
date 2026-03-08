package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.BusinessTripDetailParam;
import com.reverse.approval.internal.persistence.row.BusinessTripDetailRow;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BusinessTripDetailMapper {
    int insertBusinessTripDetail(BusinessTripDetailParam param);

    Optional<BusinessTripDetailRow> findBusinessTripDetailByApprovalId(
            @Param("approvalId") Long approvalId);
}
