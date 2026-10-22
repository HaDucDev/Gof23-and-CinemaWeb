package com.td.cinema.seat.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public class HoldCommandRequest {
    @NotNull public UUID bookingId;

    @Min(1)
    public long accountId;

    @Min(0)
    public long revision;

    public String token;
    public String reason;
}
