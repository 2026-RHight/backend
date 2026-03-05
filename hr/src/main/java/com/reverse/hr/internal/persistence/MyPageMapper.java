package com.reverse.hr.internal.persistence;

import com.reverse.hr.internal.persistence.param.CareerCreateParam;
import com.reverse.hr.internal.persistence.param.SkillCreateParam;
import com.reverse.hr.internal.persistence.param.UpdateBasicInfoParam;
import com.reverse.hr.internal.persistence.row.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MyPageMapper {

    // 기본 정보
    Optional<BasicInfoRow> findBasicInfoByEmployeeId(@Param("employeeId") Long employeeId);

    // 마이페이지 상단 헤더 정보
    Optional<MyPageHeaderRow> findMyPageHeaderByEmployeeId(@Param("employeeId") Long employeeId);

    // 인사 정보
    Optional<HrInfoRow> findHrInfoByEmployeeId(@Param("employeeId") Long employeeId);

    // 역량 정보
    List<SkillItemRow> findSkillsByEmployeeId(@Param("employeeId") Long employeeId);

    // 경력 사항
    List<CareerItemRow> findCareersByEmployeeId(@Param("employeeId") Long employeeId);

    int insertHrFile(HrFileRow hrFileRow);

    int insertSkill(SkillCreateParam param);

    int insertCareer(CareerCreateParam param);

    int updateBasicInfo(UpdateBasicInfoParam param);

    int updateProfileId(@Param("employeeId") Long employeeId, @Param("profileId") Long profileId);

    Optional<HrFileRow> findSkillFileByIdAndEmployeeId(@Param("employeeId") Long employeeId, @Param("skillId") Long skillId);

    int deleteSkillByIdAndEmployeeId(@Param("employeeId") Long employeeId, @Param("skillId") Long skillId);

    Optional<HrFileRow> findCareerFileByIdAndEmployeeId(@Param("employeeId") Long employeeId, @Param("careerId") Long careerId);

    int deleteCareerByIdAndEmployeeId(@Param("employeeId") Long employeeId, @Param("careerId") Long careerId);

    int countHrFileReferences(@Param("hrFileId") Long hrFileId);

    int deleteHrFileById(@Param("hrFileId") Long hrFileId);


}
