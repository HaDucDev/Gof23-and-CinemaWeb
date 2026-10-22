package com.td.cinema.booking.exception;

import com.td.cinema.booking.dto.response.ApiErrorResponse;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> business(ApiException e) {
        return ResponseEntity.status(e.status).body(new ApiErrorResponse(e.code, e.getMessage()));
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> invalid(Exception e) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("INVALID_REQUEST", "Du lieu gui len khong hop le."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> constraint(DataIntegrityViolationException e) {
        return ResponseEntity.status(409)
                .body(
                        new ApiErrorResponse(
                                "DATA_CONFLICT",
                                "Du lieu trung hoac khong dap ung rang buoc nghiep vu."));
    }
}
