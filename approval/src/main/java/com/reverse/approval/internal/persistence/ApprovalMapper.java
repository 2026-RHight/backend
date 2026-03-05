package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.ElectronicApprovalParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApprovalMapper {
    int insertElectronicApproval(ElectronicApprovalParam param);
}
