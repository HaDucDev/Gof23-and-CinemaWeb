package com.td.cinema.choreography.payment.model;

import com.td.cinema.choreography.payment.exception.ApiException;

public enum PaymentOutcome {
    SUCCESS,
    FAIL;

    public static PaymentOutcome parse(String value) {
        if ("SUCCESS".equals(value)) return SUCCESS;
        if ("FAIL".equals(value)) return FAIL;
        throw ApiException.bad("Chi chap nhan SUCCESS hoac FAIL.");
    }
}
