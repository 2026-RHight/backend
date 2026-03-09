package com.reverse.core.security;

import com.reverse.core.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author sekong11
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class JwtTokenProvider {

    /** Base64 인코딩된 JWT 서명용 비밀키 문자열 */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /** Access 토큰 만료 시간 (밀리초) */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /** Refresh 토큰 만료 시간 (밀리초) */
    @Value("${jwt.refresh-expiration}")
    private long jwtRefreshExpiration;

    /** Base64 디코딩 후 생성된 암호화 키 객체 */
    private SecretKey secretKey;

    /**
     * 서명용 SecretKey 초기화
     *
     * <p>Base64 문자열을 바이트 배열로 디코딩한 뒤 HMAC-SHA 키 객체로 변환한다. 애플리케이션 시작 시 한 번만 실행되며, 이후 모든 토큰 작업에서
     * 재사용된다.
     */
    @PostConstruct
    public void init() {
        // Base64 문자열을 실제 키로 변환
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access 토큰 생성
     *
     * <p>사원 PK를 subject에, 사번과 역할 목록을 claim에 담아 서명된 JWT 문자열을 반환한다.
     *
     * @param employeeId 사원 PK
     * @param employeeNum 사원번호 (로그인 ID)
     * @param roles 역할 코드 목록 (예: ["ADMIN", "MANAGER"])
     * @return 서명된 Access 토큰 문자열
     */
    public String createToken(Long employeeId, String employeeNum, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(String.valueOf(employeeId))
                .claim("employeeNum", employeeNum)
                .claim("roles", roles)
                .claim("tokenType", "ACCESS")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh 토큰 생성
     *
     * <p>Access 토큰과 동일한 claim 구조를 가지며, 만료 시간만 더 길게 설정된다. Access 토큰 만료 시 새로운 Access 토큰을 발급받기 위해
     * 사용한다.
     *
     * @param employeeId 사원 PK
     * @param employeeNum 사원번호 (로그인 ID)
     * @param roles 역할 코드 목록
     * @return 서명된 Refresh 토큰 문자열
     */
    public String createRefreshToken(Long employeeId, String employeeNum, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtRefreshExpiration);

        return Jwts.builder()
                .subject(String.valueOf(employeeId))
                .claim("employeeNum", employeeNum)
                .claim("roles", roles)
                .claim("tokenType", "REFRESH")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String createPasswordChangeTicket(Long employeeId, String employeeNum) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + 5 * 60 * 1000);
        return Jwts.builder()
                .subject(String.valueOf(employeeId))
                .claim("employeeNum", employeeNum)
                .claim("purpose", "PASSWORD_CHANGE")
                .issuedAt(now)
                .expiration(exp)
                .signWith(secretKey)
                .compact();
    }

    public String createSalaryDetailTicket(Long employeeId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + 5 * 60 * 1000); // 5 minutes
        return Jwts.builder()
                .subject(String.valueOf(employeeId))
                .claim("purpose", "SALARY_DETAIL")
                .issuedAt(now)
                .expiration(exp)
                .signWith(secretKey)
                .compact();
    }

    public void validateSalaryDetailTicket(String token, Long employeeId) {
        try {
            Claims c = parseClaims(token);
            if (!"SALARY_DETAIL".equals(c.get("purpose"))) {
                throw new UnauthorizedException("INVALID_TICKET", "유효하지 않은 급여 조회 티켓입니다.");
            }
            if (!String.valueOf(employeeId).equals(c.getSubject())) {
                throw new UnauthorizedException("FORBIDDEN", "본인의 급여 조회 티켓만 사용할 수 있습니다.");
            }
        } catch (Exception e) {
            throw new UnauthorizedException("INVALID_TICKET", "만료되었거나 유효하지 않은 급여 조회 티켓입니다.");
        }
    }

    public void validatePasswordChangeTicket(String token) {
        try {
            Claims c = parseClaims(token);
            if (!"PASSWORD_CHANGE".equals(c.get("purpose"))) {
                throw new UnauthorizedException("INVALID_TICKET", "유효하지 않은 변경 티켓입니다.");
            }
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("INVALID_TICKET", "만료된 변경 티켓입니다.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("INVALID_TICKET", "유효하지 않은 변경 티켓입니다.");
        }
    }

    public Long getEmployeeIdFromPasswordChangeTicket(String token) {
        validatePasswordChangeTicket(token);
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public String getTokenType(String token) {
        return parseClaims(token).get("tokenType", String.class);
    }

    /**
     * 토큰에서 사원 PK 추출
     *
     * <p>JWT의 subject에 저장된 사원 PK를 Long 타입으로 변환하여 반환한다.
     *
     * @param token JWT 토큰 문자열
     * @return 사원 PK
     */
    public Long getEmployeeId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    /**
     * 토큰에서 사원번호 추출
     *
     * <p>JWT claim에 저장된 사원번호(로그인 ID)를 반환한다.
     *
     * @param token JWT 토큰 문자열
     * @return 사원번호
     */
    public String getEmployeeNum(String token) {
        Claims claims = parseClaims(token);

        return claims.get("employeeNum").toString();
    }

    /**
     * 토큰에서 역할 목록 추출
     *
     * <p>JWT claim에 저장된 역할 코드 리스트를 반환한다.
     *
     * @param token JWT 토큰 문자열
     * @return 역할 코드 목록 (예: ["ADMIN", "MANAGER"])
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        return (List<String>) parseClaims(token).get("roles");
    }

    /**
     * 토큰 유효성 검증
     *
     * <p>서명 위조, 형식 오류, 만료 여부를 검사한다. 유효하지 않은 경우 BusinessException을 던진다.
     *
     * @param token JWT 토큰 문자열
     * @throws UnauthorizedException 토큰이 위조/만료/형식 오류인 경우
     */
    public void validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);

        } catch (SecurityException
                | MalformedJwtException
                | UnsupportedJwtException
                | IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid token");
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("Token is expired");
        }
    }

    /**
     * 토큰 유효 여부를 boolean으로 반환
     *
     * <p>validateToken()의 래퍼 메서드로, 예외 대신 true/false를 반환한다. JWT 인증 필터에서 간편하게 유효성을 체크할 때 사용한다.
     *
     * @param token JWT 토큰 문자열
     * @return 유효하면 true, 아니면 false
     */
    public boolean isValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (UnauthorizedException e) {
            return false;
        }
    }

    /**
     * 토큰을 파싱하여 Claims 객체를 반환하는 내부 메서드
     *
     * <p>서명 검증 후 토큰 payload에 담긴 정보(Claims)를 꺼낸다. 모든 get 메서드들이 이 메서드를 통해 토큰 정보에 접근한다.
     *
     * @param token JWT 토큰 문자열
     * @return 토큰에 담긴 Claims 객체
     */
    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }
}
