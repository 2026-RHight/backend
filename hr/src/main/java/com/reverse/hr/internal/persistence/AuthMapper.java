package com.reverse.hr.internal.persistence;

import com.reverse.hr.internal.persistence.row.InitializeUserRow;
import com.reverse.hr.internal.persistence.row.LoginProfileRow;
import com.reverse.hr.internal.persistence.row.LoginUserRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AuthMapper {

    Optional<LoginUserRow> findUserByEmployeeNum(@Param("employeeNum") String employeeNum);

    Optional<LoginUserRow> findUserByEmployeeId(@Param("employeeId") Long employeeId);

    Optional<LoginProfileRow> findLoginProfileByEmployeeId(Long employeeId);

    List<String> findRoleCodesByEmployeeId(@Param("employeeId") Long employeeId);

    Optional<InitializeUserRow> findInitializeUserByEmployeeNum(@Param("employeeNum") String employeeNum);

    int updatePasswordAndInitialState(
            @Param("employeeId") Long employeeId,
            @Param("password") String password,
            @Param("initialState") boolean initialState
    );

    int insertPasswordHistory(
            @Param("employeeId") Long employeeId,
            @Param("passwordHash") String passwordHash
    );
}
