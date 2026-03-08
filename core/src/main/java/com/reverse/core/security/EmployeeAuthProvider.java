package com.reverse.core.security;

import java.util.List;
import java.util.Optional;

/**
 * 사원 인증 정보 조회 인터페이스
 *
 * <p>core 모듈에서 정의하고, 도메인 모듈(hr)에서 구현한다. core가 도메인 모듈을 직접 의존하지 않기 위한 구조.
 */
public interface EmployeeAuthProvider {
    Optional<EmployeeAuthInfoDTO> findByEmployeeNum(String employeeNum);

    List<String> findRolesByEmployeeId(Long employeeId);
}
