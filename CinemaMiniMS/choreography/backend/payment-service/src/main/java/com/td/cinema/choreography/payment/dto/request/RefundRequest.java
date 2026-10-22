package com.td.cinema.choreography.payment.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public class RefundRequest {
    @NotNull public UUID bookingId;
    @NotBlank public String operationId;
}
