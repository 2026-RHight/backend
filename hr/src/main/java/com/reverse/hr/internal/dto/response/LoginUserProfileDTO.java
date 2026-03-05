package com.reverse.hr.internal.dto.response;

/**
 * 로그인 성공 시 클라이언트에 내려주는 사용자 프로필 요약 정보 DTO.
 */
public record LoginUserProfileDTO(
        Long employeeId,
        String employeeNum,
        String employeeName,
        String orgName,
        String positionName,
        String rankName,
        String jobName
) {}
