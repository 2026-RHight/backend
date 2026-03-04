package com.reverse.hr.internal.application;

import com.reverse.core.exception.UnauthorizedException;
import com.reverse.core.security.JwtTokenProvider;
import com.reverse.hr.internal.application.dto.request.ChangePasswordRequestDTO;
import com.reverse.hr.internal.application.dto.request.InitializeRequestDTO;
import com.reverse.hr.internal.application.dto.request.LoginRequestDTO;
import com.reverse.hr.internal.application.dto.response.LoginResponseDTO;
import com.reverse.hr.internal.application.dto.response.LoginUserProfileDTO;
import com.reverse.hr.internal.exception.AuthErrorCode;
import com.reverse.hr.internal.persistence.AuthMapper;
import com.reverse.hr.internal.persistence.row.InitializeUserRow;
import com.reverse.hr.internal.persistence.row.LoginProfileRow;
import com.reverse.hr.internal.persistence.row.LoginUserRow;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ResidentHashService residentHashService;

    private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghjkmnpqrstuvwxyz";
    private static final String DIGIT = "23456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{}?";
    private static final String ALL = UPPER + LOWER + DIGIT + SPECIAL;

    /**
     * 사번/비밀번호를 검증하고 로그인 응답(토큰 또는 비밀번호 변경 티켓)을 반환한다.
     *
     * @param request 로그인 요청 정보
     * @return 로그인 결과
     */
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
            return new LoginResponseDTO(true,null,ticket,null);
        }
        List<String> roles = authMapper.findRoleCodesByEmployeeId(user.employeeId());

        String accessToken = jwtTokenProvider.createToken(user.employeeId(),user.employeeNum(),roles);

        LoginProfileRow profileRow = authMapper.findLoginProfileByEmployeeId(user.employeeId())
                .orElseThrow(() -> new IllegalStateException("로그인 프로필 정보를 찾을 수 없습니다."));

        LoginUserProfileDTO profile = new LoginUserProfileDTO(
                profileRow.employeeId(),
                profileRow.employeeNum(),
                profileRow.employeeName(),
                profileRow.orgName(),
                profileRow.positionName(),
                profileRow.rankName(),
                profileRow.jobName()
        );

        return new LoginResponseDTO(false,accessToken,null, profile);
    }

    /**
     * 사번과 주민번호 해시 검증 후 비밀번호를 초기 상태로 재설정한다.
     *
     * @param dto 비밀번호 초기화 요청 정보
     */
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

        // TODO(클로이): 이메일 전송 로직 추가 후 사번 초기화 삭제
//        // 5) 임시 비밀번호 생성 후 이메일 전송 방식 추후
//        String tempPassword = generateTempPassword(); // 12~16자, 영문+숫자+특수
//        String encoded = passwordEncoder.encode(tempPassword);

//        int updated = authMapper.updatePasswordAndInitialState(
//                user.employeeId(),
//                passwordEncoder,
//                true
//        );
//
//        int inserted = authMapper.insertPasswordHistory(
//                user.employeeId(),
//                passwordEncoder
//        );

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

    /**
     * 비밀번호 변경 티켓을 검증하고 초기 비밀번호를 새 비밀번호로 변경한다.
     *
     * @param ticket 비밀번호 변경 티켓
     * @param request 비밀번호 변경 요청
     * @return 변경 완료 후 로그인 응답
     */
    @Transactional
    public LoginResponseDTO changeInitialPassword(String ticket, ChangePasswordRequestDTO request){
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

        LoginProfileRow profileRow = authMapper.findLoginProfileByEmployeeId(user.employeeId())
                .orElseThrow(() -> new IllegalStateException("로그인 프로필 정보를 찾을 수 없습니다."));

        LoginUserProfileDTO profile = new LoginUserProfileDTO(
                profileRow.employeeId(),
                profileRow.employeeNum(),
                profileRow.employeeName(),
                profileRow.orgName(),
                profileRow.positionName(),
                profileRow.rankName(),
                profileRow.jobName()
        );

        return new LoginResponseDTO(false,accessToken,null, profile);
    }

    /**
     * 임시 비밀번호를 생성한다.
     *
     * @return 생성된 임시 비밀번호
     */
    private String generateTempPassword() {
        int length = 12; // 8~15 정책 충족
        char[] password = new char[length];

        // 최소 1개씩 보장
        password[0] = UPPER.charAt(SECURE_RANDOM.nextInt(UPPER.length()));
        password[1] = LOWER.charAt(SECURE_RANDOM.nextInt(LOWER.length()));
        password[2] = DIGIT.charAt(SECURE_RANDOM.nextInt(DIGIT.length()));
        password[3] = SPECIAL.charAt(SECURE_RANDOM.nextInt(SPECIAL.length()));

        for (int i = 4; i < length; i++) {
            password[i] = ALL.charAt(SECURE_RANDOM.nextInt(ALL.length()));
        }

        // Fisher-Yates shuffle
        for (int i = password.length - 1; i > 0; i--) {
            int j = SECURE_RANDOM.nextInt(i + 1);
            char tmp = password[i];
            password[i] = password[j];
            password[j] = tmp;
        }

        return new String(password);
    }

}
