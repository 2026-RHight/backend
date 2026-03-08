package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.RecipientLineParam;
import com.reverse.approval.internal.persistence.row.RecipientLineDetailRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RecipientLineMapper {
    int insertRecipientLine(RecipientLineParam param);

    List<Long> findRecipientIdsByApprovalId(@Param("approvalId") Long approvalId);

    List<RecipientLineDetailRow> findRecipientLinesByApprovalId(
            @Param("approvalId") Long approvalId);

    int countByApprovalIdAndReceiverId(
            @Param("approvalId") Long approvalId, @Param("receiverId") Long receiverId);
}
