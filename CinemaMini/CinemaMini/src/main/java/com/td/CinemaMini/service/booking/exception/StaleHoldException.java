package com.td.CinemaMini.service.booking.exception;

/**
 * Báo ghế đang thuộc một phiên khác đã quá hạn để SeatHoldService dọn phiên đó rồi thử lại.
 * Exception làm lần giữ ghế hiện tại rollback, nhả khóa trước khi dọn phiên cũ.
 */
public class StaleHoldException extends RuntimeException {
    /** ID phiên giữ ghế cũ cần được dọn; không phải ID ghế hoặc ID đơn đặt vé. */
    private final Long holdId;

    public StaleHoldException(Long holdId) {
        this.holdId = holdId;
    }

    public Long getHoldId() {
        return holdId;
    }
}
