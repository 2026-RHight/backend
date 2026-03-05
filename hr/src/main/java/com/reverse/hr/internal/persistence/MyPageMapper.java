package com.reverse.hr.internal.persistence;

import com.reverse.hr.internal.persistence.param.SkillCreateParam;
import com.reverse.hr.internal.persistence.row.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MyPageMapper {

    // 기본 정보
    Optional<BasicInfoRow> findBasicInfoByEmployeeId(@Param("employeeId") Long employeeId);

    // 인사 정보
    Optional<HrInfoRow> findHrInfoByEmployeeId(@Param("employeeId") Long employeeId);

    // 역량 정보
    List<SkillItemRow> findSkillsByEmployeeId(@Param("employeeId") Long employeeId);

    // 경력 사항
    List<CareerItemRow> findCareersByEmployeeId(@Param("employeeId") Long employeeId);

    int insertHrFile(HrFileRow hrFileRow);

    int insertSkill(SkillCreateParam param);

}
