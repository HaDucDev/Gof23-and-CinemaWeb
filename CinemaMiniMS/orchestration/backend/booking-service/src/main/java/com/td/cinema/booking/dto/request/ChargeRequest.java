package com.td.cinema.booking.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public class ChargeRequest {
    @NotNull public UUID bookingId;

    @NotNull
    @DecimalMin("1")
    public BigDecimal amount;

    @NotBlank public String paymentMethod;
    @NotBlank public String operationId;
}
