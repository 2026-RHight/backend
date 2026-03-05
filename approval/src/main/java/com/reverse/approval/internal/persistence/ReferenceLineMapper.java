package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.ReferenceLineParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReferenceLineMapper {
    int insertReferenceLine(ReferenceLineParam param);
}
