package com.td.cinema.choreography.booking.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public class BookingCreateRequest {
    @NotNull public UUID holdId;
    @NotBlank public String token;

    @Min(0)
    public long revision;

    @NotBlank
    @Size(max = 120)
    public String contactName;

    @NotBlank
    @Email
    @Size(max = 254)
    public String contactEmail;

    @Size(max = 30)
    public String contactPhone;

    @Size(max = 1000)
    public String note;
}
