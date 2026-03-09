package com.reverse.core.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SequenceMapper {
    int incrementSequenceNumber(@Param("prefix") String prefix, @Param("doc_year") String docYear);

    void insertInitialSequence(@Param("prefix") String prefix, @Param("doc_year") String docYear);

    int getLastInsertId();
}
