package com.td.CinemaMini.model;

import com.td.CinemaMini.model.enums.SeatType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Một ghế trong phòng chiếu; cùng ghế này có trạng thái bán vé riêng ở từng suất trong
 * ShowtimeSeat.
 *
 * Các field lưu ID của bảng khác chỉ chứa số ID. Service kiểm tra bản ghi liên quan; class không
 * khai báo quan hệ JPA hoặc tạo khóa ngoại.
 */
@Entity
@Table(name = "seat")
public class Seat {
    /** ID ghế trong phòng chiếu; các suất chiếu dùng lại ID ghế này. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** Mã ghế hiển thị cho khách, ghép tên hàng và số ghế, ví dụ A1 hoặc E6. */
    @Column(name = "code", nullable = false, length = 3)
    private String code;

    /** Tên hàng ghế trong phòng: A, B, C, D hoặc E. */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "row_label", nullable = false, length = 1, columnDefinition = "char(1)")
    private String rowLabel;

    /** Số thứ tự của ghế trong hàng, từ 1 đến 6. */
    @Column(name = "seat_number", nullable = false)
    private Short seatNumber;

    /** Loại ghế: STANDARD cho hàng A-C, VIP cho hàng D-E; dùng để tính phụ thu. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "seat_type", nullable = false, length = 16)
    private SeatType seatType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public void setRowLabel(String rowLabel) {
        this.rowLabel = rowLabel;
    }

    public Short getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Short seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }
}
