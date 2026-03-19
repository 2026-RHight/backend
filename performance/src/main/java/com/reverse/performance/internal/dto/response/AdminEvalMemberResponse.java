package com.reverse.performance.internal.dto.response;

public record AdminEvalMemberResponse(
        Long employeeId, String name, String position, Boolean hasFinalScore, String finalGrade) {}
