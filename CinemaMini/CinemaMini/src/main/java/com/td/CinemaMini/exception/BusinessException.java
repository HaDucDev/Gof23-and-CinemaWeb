package com.td.CinemaMini.exception;

/**
 * Lỗi nghiệp vụ dùng chung cho tài khoản, đặt vé, lịch chiếu, thanh toán và thông báo.
 * ErrorHandler dùng mã lỗi, thông báo và HTTP status của lỗi này để trả kết quả cho người dùng.
 */
public class BusinessException extends RuntimeException {
    private final int status;
    private final String code;

    public BusinessException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public static BusinessException missing() {
        return new BusinessException(404, "NOT_FOUND", "Không tìm thấy dữ liệu.");
    }

    public static BusinessException forbidden() {
        return new BusinessException(
                403, "FORBIDDEN", "Bạn không có quyền thực hiện thao tác này.");
    }

    public static BusinessException invalid(String message) {
        return new BusinessException(400, "INVALID_INPUT", message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(409, "CONFLICT", message);
    }
}
