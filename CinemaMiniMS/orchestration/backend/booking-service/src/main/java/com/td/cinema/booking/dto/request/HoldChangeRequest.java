package com.td.cinema.booking.dto.request;

import jakarta.validation.constraints.*;

import java.util.List;

public class HoldChangeRequest extends HoldReleaseRequest {
    @NotNull public List<String> seatNos;
}
