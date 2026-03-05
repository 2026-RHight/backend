package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.ApprovalLineParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApprovalLineMapper {
    int insertApprovalLine(ApprovalLineParam param);
}
