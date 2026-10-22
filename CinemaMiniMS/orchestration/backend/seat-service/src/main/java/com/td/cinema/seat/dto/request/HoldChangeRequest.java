package com.td.cinema.seat.dto.request;

import jakarta.validation.constraints.*;

import java.util.List;

public class HoldChangeRequest extends HoldReleaseRequest {
    @NotNull public List<String> seatNos;
}
