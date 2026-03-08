package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.ElectronicApprovalParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApprovalMapper {
    int insertElectronicApproval(ElectronicApprovalParam param);

    int countByApprovalId(@Param("approvalId") Long approvalId);

    int countByApprovalIdAndDrafterId(
            @Param("approvalId") Long approvalId, @Param("drafterId") Long drafterId);

    int deleteElectronicApprovalById(@Param("approvalId") Long approvalId);
}
