package com.reverse.hr.internal.domain.enums;

public enum SensitiveFieldType {
    RESIDENT_NUMBER,
    ACCOUNT_NUMBER;

    public static SensitiveFieldType from(String value) {
        try {
            return SensitiveFieldType.valueOf(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("지원하지 않는 조회 항목입니다: " + value);
        }
    }
}
