package com.td.CinemaMini.service.booking;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Component;

/** Định dạng tiền và thời gian để dùng trong các trang HTML. */
@Component("display")
public class DisplayService {
    public String money(BigDecimal amount) {
        // Hiển thị tiền đồng theo cách phân nhóm số tiếng Việt; giá trị null được xem là 0.
        return NumberFormat.getIntegerInstance(new Locale("vi", "VN"))
                        .format(amount == null ? BigDecimal.ZERO : amount)
                + " đ";
    }

    /** Hiển thị cả ngày và giờ Việt Nam; dùng dấu gạch khi chưa có thời điểm. */
    public String date(Instant instant) {
        return instant == null
                ? "—"
                : DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                        .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                        .format(instant);
    }

    /** Lấy giờ và phút theo múi giờ Việt Nam để hiển thị giờ chiếu. */
    public String time(Instant instant) {
        return DateTimeFormatter.ofPattern("HH:mm")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(instant);
    }

    /** Lấy ngày theo múi giờ Việt Nam để hiển thị ngày chiếu. */
    public String day(Instant instant) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(instant);
    }
}
