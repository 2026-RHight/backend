package com.reverse.approval.internal.persistence;

import com.reverse.approval.internal.persistence.param.BusinessTripDetailParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BusinessTripDetailMapper {
    int insertBusinessTripDetail(BusinessTripDetailParam param);
}
