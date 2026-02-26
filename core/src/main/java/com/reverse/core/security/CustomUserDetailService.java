package com.reverse.core.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author sekong11
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    // Mapper가 아닌 인터페이스 의존
    private final EmployeeAuthProvider employeeAuthProvider;

    @Override
    public UserDetails loadUserByUsername(String employeeNum) throws UsernameNotFoundException {
        EmployeeAuthInfoDTO employee =
                employeeAuthProvider
                        .findByEmployeeNum(employeeNum)
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "사원을 찾을 수 없습니다: \" + employeeNum"));

        List<String> roles = employeeAuthProvider.findRolesByEmployeeId(employee.employeeId());

        List<SimpleGrantedAuthority> authorities =
                roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();

        return new CustomUser(
                employee.employeeId(),
                employee.employeeNum(),
                authorities,
                employee.password());
    }
}
