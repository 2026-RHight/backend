package com.reverse.hr.internal.application;

import com.reverse.core.security.EmployeeAuthInfoDTO;
import com.reverse.core.security.EmployeeAuthProvider;
import com.reverse.hr.HrFacade;
import com.reverse.hr.internal.persistence.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HrServiceImpl implements EmployeeAuthProvider, HrFacade {

    private final EmployeeMapper employeeMapper;

    // === EmployeeAuthProvider 구현 (core용) ===

    @Override
    public Optional<EmployeeAuthInfoDTO> findByEmployeeNum(String employeeNum) {
        return employeeMapper.findUserByEmployeeNum(employeeNum);
    }

    @Override
    public List<String> findRolesByEmployeeId(Long employeeId) {
        return employeeMapper.findRolesByEmployeeId(employeeId);
    }

    // === HrFacade 구현 (다른 도메인 모듈용) ===
    // 나중에 다른 모듈이 필요한 메서드 추가
}