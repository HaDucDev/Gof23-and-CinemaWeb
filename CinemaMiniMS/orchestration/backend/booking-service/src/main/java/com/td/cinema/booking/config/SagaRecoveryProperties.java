package com.td.cinema.booking.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/** Cấu hình tiếp tục hủy vé đã thanh toán (CANCEL_PAID) khi còn bước chưa hoàn tất. */
@Component
@Validated
@ConfigurationProperties(prefix = "cinema.saga.recovery")
public class SagaRecoveryProperties {
    /** Khoảng nghỉ giữa các lần quét công việc đến hạn, tính bằng mili giây. */
    @Min(50) private long scanDelayMs = 2000;
    /** Sau khi ghi lỗi tạm thời, chờ ít nhất khoảng này mới được thử lại bước đó. */
    @Min(50) private long retryDelayMs = 5000;
    /** Thời hạn quyền xử lý của một lượt; lượt bị gián đoạn có thể được nhận lại sau khi hết hạn. */
    @Min(1000) private long leaseMs = 60000;
    /** Số lần thử tối đa cho mỗi bước, gồm lần đầu; chuyển sang bước mới thì đếm lại từ đầu. */
    @Min(1) private int maxAttempts = 12;
    /** Số đơn đến hạn được tìm trong một lần quét, không phải số luồng chạy song song. */
    @Min(1) @Max(100) private int batchSize = 20;

    public long getScanDelayMs() { return scanDelayMs; }
    public void setScanDelayMs(long value) { scanDelayMs = value; }
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long value) { retryDelayMs = value; }
    public long getLeaseMs() { return leaseMs; }
    public void setLeaseMs(long value) { leaseMs = value; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int value) { maxAttempts = value; }
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int value) { batchSize = value; }
}
