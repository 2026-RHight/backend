package com.reverse.hr.internal.application;

import com.reverse.core.exception.UnauthorizedException;
import com.reverse.core.response.ApiResponse;
import com.reverse.core.security.JwtTokenProvider;
import com.reverse.hr.internal.application.dto.request.ChangePasswordRequestDTO;
import com.reverse.hr.internal.application.dto.request.InitializeRequestDTO;
import com.reverse.hr.internal.application.dto.request.LoginRequestDTO;
import com.reverse.hr.internal.application.dto.response.LoginResponseDTO;
import com.reverse.hr.internal.application.dto.response.TokenResponseDTO;
import com.reverse.hr.internal.domain.enums.EmployeeState;
import com.reverse.hr.internal.exception.AuthErrorCode;
import com.reverse.hr.internal.persistence.AuthMapper;
import com.reverse.hr.internal.persistence.row.InitializeUserRow;
import com.reverse.hr.internal.persistence.row.LoginUserRow;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ResidentHashService residentHashService;

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {

        LoginUserRow user = authMapper.findUserByEmployeeNum(request.employeeNum())
                .orElseThrow(() -> new UnauthorizedException(
                        AuthErrorCode.AUTH_LOGIN_FAILED,"아이디 또는 비밀번호가 올바르지 않습니다."));

        if(!passwordEncoder.matches(request.password(),user.password())){
            throw new UnauthorizedException(
                    AuthErrorCode.AUTH_LOGIN_FAILED, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        if (Boolean.TRUE.equals(user.initialState())){
            String ticket = jwtTokenProvider.createPasswordChangeTicket(user.employeeId(),user.employeeNum());
            return new LoginResponseDTO(true,null,ticket);
        }
        List<String> roles = authMapper.findRoleCodesByEmployeeId(user.employeeId());

        String accessToken = jwtTokenProvider.createToken(user.employeeId(),user.employeeNum(),roles);

        return new LoginResponseDTO(false,accessToken,null);
    }

    @Transactional
    public void initializePassword(InitializeRequestDTO dto) {
        // 1) 사번으로 사용자 조회 (초기화용 row: employeeId, employeeNum, residentNumberHash 필요)
        InitializeUserRow user = authMapper.findInitializeUserByEmployeeNum(dto.employeeNum())
                .orElseThrow(() -> new UnauthorizedException(
                        AuthErrorCode.AUTH_LOGIN_FAILED,
                        "인증 정보가 올바르지 않습니다."
                ));

        // 2) 입력 주민번호 정규화 (숫자만)
        String normalizedResidentNum = dto.residentNum().replaceAll("[^0-9]", "");

        // 3) 입력값 해시 계산 (저장 시와 동일 로직/pepper 사용)
        String inputHash = residentHashService.hash(normalizedResidentNum);

        // 4) 주민번호 검증
        if (!inputHash.equals(user.residentNumberHash())) {
            throw new UnauthorizedException(
                    AuthErrorCode.AUTH_LOGIN_FAILED,
                    "인증 정보가 올바르지 않습니다."
            );
        }

        // 5) 비밀번호를 사번으로 초기화(평문 저장 금지)
        String encodedInitPassword = passwordEncoder.encode(user.employeeNum());

        int updated = authMapper.updatePasswordAndInitialState(
                user.employeeId(),
                encodedInitPassword,
                true
        );

        int inserted = authMapper.insertPasswordHistory(
                user.employeeId(),
                encodedInitPassword
        );

        if (updated != 1 || inserted != 1) {
            throw new IllegalStateException("비밀번호 초기화 처리 중 오류가 발생했습니다.");
        }

    }

    @Transactional
    public LoginResponseDTO changeInitialPassword(String ticket, ChangePasswordRequestDTO request){
        jwtTokenProvider.validatePasswordChangeTicket(ticket);
        Long employeeId = jwtTokenProvider.getEmployeeIdFromPasswordChangeTicket(ticket);

        LoginUserRow user = authMapper.findUserByEmployeeId(employeeId)
                .orElseThrow(() -> new UnauthorizedException(
                        AuthErrorCode.AUTH_LOGIN_FAILED,
                        "인증 정보가 올바르지 않습니다."
                ));

        if (!Boolean.TRUE.equals(user.initialState())) {
            throw new UnauthorizedException(
                    AuthErrorCode.AUTH_LOGIN_FAILED,
                    "인증 정보가 올바르지 않습니다.");
        }

        // 새 비밀번호 확인값 검증
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new UnauthorizedException(
                    AuthErrorCode.INVALID_PASSWORD_CONFIRM,
                    "새 비밀번호와 비밀번호 확인이 일치하지 않습니다."
            );
        }

        // 기존 비밀번호와 동일 사용 금지
        if (passwordEncoder.matches(request.newPassword(), user.password())) {
            throw new UnauthorizedException(
                    AuthErrorCode.INVALID_NEW_PASSWORD,
                    "기존 비밀번호와 다른 비밀번호를 입력해주세요."
            );
        }

        String encodedNewPassword = passwordEncoder.encode(request.newPassword());

        int updated = authMapper.updatePasswordAndInitialState(
                user.employeeId(),
                encodedNewPassword,
                false
        );
        int inserted = authMapper.insertPasswordHistory(
                user.employeeId(),
                encodedNewPassword
        );

        if (updated != 1 || inserted != 1) {
            throw new IllegalStateException("비밀번호 변경 처리 중 오류가 발생했습니다.");
        }

        List<String> roles = authMapper.findRoleCodesByEmployeeId(user.employeeId());
        String accessToken = jwtTokenProvider.createToken(user.employeeId(),user.employeeNum(),roles);

        return new LoginResponseDTO(false,accessToken,null);
    }
}
