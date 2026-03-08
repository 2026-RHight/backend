package com.reverse.payroll.internal.exception;

import com.reverse.core.exception.UnauthorizedException;

public class InvalidSalaryPasswordException extends UnauthorizedException {

    public InvalidSalaryPasswordException(String message) {
        super(PayrollErrorCode.INVALID_PASSWORD.getCode(), message);

    }


}
