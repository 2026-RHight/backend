package com.reverse.hr.internal.persistence;

import com.reverse.hr.internal.persistence.row.InitializeUserRow;
import com.reverse.hr.internal.persistence.row.LoginProfileRow;
import com.reverse.hr.internal.persistence.row.LoginUserRow;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {

    /**
     * 사번으로 로그인 대상 사용자를 조회한다.
     *
     * @param employeeNum 사번
     * @return 로그인 사용자 정보
     */
    Optional<LoginUserRow> findUserByEmployeeNum(@Param("employeeNum") String employeeNum);

    /**
     * 사원 ID로 로그인 대상 사용자를 조회한다.
     *
     * @param employeeId 사원 ID
     * @return 로그인 사용자 정보
     */
    Optional<LoginUserRow> findUserByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 로그인 화면/헤더 표시용 사원 프로필 요약 정보를 조회한다.
     *
     * @param employeeId 사원 ID
     * @return 로그인 프로필 요약 정보
     */
    Optional<LoginProfileRow> findLoginProfileByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 사원 ID로 역할 코드 목록을 조회한다.
     *
     * @param employeeId 사원 ID
     * @return 역할 코드 목록
     */
    List<String> findRoleCodesByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 비밀번호 초기화 검증용 사용자 정보를 사번으로 조회한다.
     *
     * @param employeeNum 사번
     * @return 초기화 대상 사용자 정보
     */
    Optional<InitializeUserRow> findInitializeUserByEmployeeNum(
            @Param("employeeNum") String employeeNum);

    /**
     * 사용자 비밀번호와 초기 상태 값을 업데이트한다.
     *
     * @param employeeId 사원 ID
     * @param password 비밀번호 해시
     * @param initialState 초기 상태 여부
     * @return 반영된 row 수
     */
    int updatePasswordAndInitialState(
            @Param("employeeId") Long employeeId,
            @Param("password") String password,
            @Param("initialState") boolean initialState);

    /**
     * 비밀번호 변경 이력을 저장한다.
     *
     * @param employeeId 사원 ID
     * @param passwordHash 비밀번호 해시
     * @return 반영된 row 수
     */
    int insertPasswordHistory(
            @Param("employeeId") Long employeeId, @Param("passwordHash") String passwordHash);
}
