package com.td.CinemaMini.model;

/** Thông tin mở bán của một ghế trong mẫu suất chiếu, dùng khi tạo suất mới bằng Prototype. */
public class SeatConfiguration {
    /** Mã ghế trong mẫu, ví dụ A1; dùng tìm Seat tương ứng khi tạo danh sách ghế cho suất mới. */
    private String code;

    /** Có mở bán ghế này cho suất mới không: true tạo ghế AVAILABLE, false tạo ghế DISABLED. */
    private boolean openSale = true;

    public String getCode() {
        return code;
    }

    public void setCode(String v) {
        code = v;
    }

    public boolean isOpenSale() {
        return openSale;
    }

    public void setOpenSale(boolean v) {
        openSale = v;
    }

    public SeatConfiguration copy() {
        SeatConfiguration copy = new SeatConfiguration();
        copy.code = code;
        copy.openSale = openSale;
        return copy;
    }
}
