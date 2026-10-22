package com.td.CinemaMini.service.booking.exception;

import com.td.CinemaMini.exception.BusinessException;

/**
 * Báo phiên giữ ghế đã hết hạn với mã HOLD_EXPIRED và HTTP 410.
 * Các service khai báo noRollbackFor để vẫn lưu việc trả ghế khi báo lỗi này.
 */
public class HoldExpiredException extends BusinessException {
    public HoldExpiredException() {
        // Gửi mã lỗi và thông báo hết hạn để màn hình yêu cầu khách chọn ghế lại.
        super(410, "HOLD_EXPIRED", "Đã hết thời gian giữ ghế. Vui lòng chọn lại.");
    }
}
