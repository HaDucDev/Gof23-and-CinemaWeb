package com.td.cinema.booking.dto.request;

import jakarta.validation.constraints.*;

public class CancelRequest {
    @Size(max = 500)
    public String reason;
}
