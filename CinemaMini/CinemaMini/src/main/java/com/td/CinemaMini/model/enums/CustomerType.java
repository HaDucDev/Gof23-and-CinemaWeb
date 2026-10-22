package com.td.CinemaMini.model.enums;

/** Nhóm khách dùng chọn chính sách giảm giá, tách riêng với quyền USER hoặc ADMIN. */
public enum CustomerType {
    /** Khách thường, không giảm giá. */
    REGULAR("Khách thường"),
    /** Khách sinh viên, giảm 10% theo chính sách hiện tại. */
    STUDENT("Sinh viên"),
    /** Khách thành viên, giảm 15% theo chính sách hiện tại. */
    MEMBER("Thành viên");
    /** Tên nhóm khách hiển thị trên giao diện, ví dụ "Sinh viên". */
    private final String label;

    CustomerType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
