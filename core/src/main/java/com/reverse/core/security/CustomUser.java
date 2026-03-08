package com.reverse.core.security;

import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUser implements UserDetails {

    private final Long employeeId;
    private final String employeeNum;
    private final String password;
    private final List<SimpleGrantedAuthority> authorities;

    public CustomUser(
            Long employeeId,
            String employeeNum,
            List<SimpleGrantedAuthority> authorities,
            String password) {
        this.employeeId = employeeId;
        this.employeeNum = employeeNum;
        this.authorities = authorities;
        this.password = password;
    }

    /** Spring Security가 로그인 식별자로 사용하는 값 우리 시스템에서는 사번(employeeNum)이 로그인 ID */
    @Override
    public String getUsername() {
        return employeeNum;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
