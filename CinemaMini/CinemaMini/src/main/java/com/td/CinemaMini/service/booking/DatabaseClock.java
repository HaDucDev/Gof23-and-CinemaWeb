package com.td.CinemaMini.service.booking;

import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Cung cấp thời gian hiện tại từ database để các bước giữ ghế dùng cùng một nguồn giờ. */
@Component
public class DatabaseClock {
    private final JdbcTemplate jdbc;

    public DatabaseClock(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Đọc sau khi chờ khóa; không dùng CURRENT_TIMESTAMP cố định đầu transaction. */
    public Instant now() {
        // clock_timestamp() lấy giờ tại lúc truy vấn, kể cả khi transaction đã chờ khóa trước đó.
        return jdbc.queryForObject("select clock_timestamp()", Timestamp.class).toInstant();
    }
}
