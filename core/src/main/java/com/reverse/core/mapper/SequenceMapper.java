package com.reverse.core.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SequenceMapper {
    int getSequenceNumber(@Param("prefix") String prefix, @Param("year") String year);

    int updateSequenceNumber(@Param("prefix") String prefix, @Param("year") String year);

    void insertInitialSequence(@Param("prefix") String prefix, @Param("year") String year);
}
