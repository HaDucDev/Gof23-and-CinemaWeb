package com.td.CinemaMini.service.booking.scheduling;

import com.td.CinemaMini.model.enums.HoldStatus;
import com.td.CinemaMini.repository.HoldSessionRepository;
import com.td.CinemaMini.service.booking.*;
import java.util.Arrays;
import org.slf4j.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Tác vụ nền tìm và trả ghế của các phiên quá hạn, kể cả khi khách đã đóng trình duyệt. */
@Component
public class HoldExpirationJob {
    private static final Logger log = LoggerFactory.getLogger(HoldExpirationJob.class);
    private final HoldSessionRepository holds;
    private final DatabaseClock clock;
    private final HoldExpirationService expiration;

    public HoldExpirationJob(
            HoldSessionRepository holds, DatabaseClock clock, HoldExpirationService expiration) {
        this.holds = holds;
        this.clock = clock;
        this.expiration = expiration;
    }

    // Bắt đầu sau 1 giây; mỗi lượt tiếp theo chạy sau khi lượt trước hoàn tất thêm 1 giây.
    @Scheduled(fixedDelay = 1000, initialDelay = 1000)
    public void sweep() {
        try {
            // Mỗi lượt lấy tối đa 50 phiên ACTIVE/CHECKOUT quá hạn theo giờ database.
            for (Long id :
                    holds.findExpiredIds(
                            Arrays.asList(HoldStatus.ACTIVE, HoldStatus.CHECKOUT),
                            clock.now(),
                            PageRequest.of(0, 50))) {
                try {
                    // Xử lý từng phiên trong transaction riêng để lỗi một phiên không hủy cả lượt
                    // quét.
                    expiration.expire(id);
                } catch (RuntimeException error) {
                    // Ghi nhận lỗi và tiếp tục phiên kế tiếp; phiên lỗi có thể được tìm lại ở lượt
                    // quét sau.
                    log.warn("Chưa xử lý được phiên hết hạn {}", id, error);
                }
            }
        } catch (RuntimeException error) {
            log.warn("Không thể quét phiên hết hạn", error);
        }
    }
}
