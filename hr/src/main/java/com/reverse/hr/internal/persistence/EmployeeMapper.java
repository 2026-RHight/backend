package com.reverse.hr.internal.persistence;

import com.reverse.core.security.EmployeeAuthInfoDTO;
import com.reverse.hr.internal.persistence.row.LoginViewRow;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmployeeMapper {

    /** 사번으로 사원 인증 정보 조회 */
    Optional<EmployeeAuthInfoDTO> findUserByEmployeeNum(@Param("employeeNum") String employeeNum);

    /** 사원 PK로 역할 코드 목록 조회 */
    List<String> findRolesByEmployeeId(@Param("employeeId") Long employeeId);

    /** 역할 PK로 접근 가능한 화면 목록 조회 */
    List<LoginViewRow> findViewsByRoleId(@Param("roleId") Long roleId);
}
