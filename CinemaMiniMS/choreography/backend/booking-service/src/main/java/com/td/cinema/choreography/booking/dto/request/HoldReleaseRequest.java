package com.td.cinema.choreography.booking.dto.request;

import jakarta.validation.constraints.*;

public class HoldReleaseRequest {
    @NotBlank public String token;

    @Min(0)
    public long revision;
}
