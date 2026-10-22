package com.td.cinema.choreography.seat.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HoldResponse {
    public String holdId;
    public String bookingId;
    public Long accountId;
    public String showtimeId;
    public String status;
    public long revision;
    public String startedAt;
    public String expiresAt;
    public String updatedAt;
    public String token;
    public List<Map<String, Object>> seats;
    public Map<String, Object> showtime;
}
