package com.td.cinema.booking.dto.request;

import jakarta.validation.constraints.*;

public class HoldReleaseRequest {
    @NotBlank public String token;

    @Min(0)
    public long revision;
}
