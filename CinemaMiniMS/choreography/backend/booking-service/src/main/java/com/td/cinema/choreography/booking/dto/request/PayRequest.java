package com.td.cinema.choreography.booking.dto.request;

import jakarta.validation.constraints.*;

public class PayRequest {
    @NotBlank public String paymentMethod;
}
