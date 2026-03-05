package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.RecipientLineParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecipientLineMapper {
    int insertRecipientLine(RecipientLineParam param);
}
