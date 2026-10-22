package com.td.CinemaMini.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Thông tin phim để hiển thị và dùng khi xếp lịch chiếu.
 *
 * Các field lưu ID của bảng khác chỉ chứa số ID. Service kiểm tra bản ghi liên quan; class không
 * khai báo quan hệ JPA hoặc tạo khóa ngoại.
 */
@Entity
@Table(name = "movie")
public class Movie {
    /** ID bộ phim trong bảng movie. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** Tên phim hiển thị trên danh sách phim, suất chiếu và thông tin vé. */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /** Thời lượng phim tính bằng phút; dùng tính giờ kết thúc khi tạo suất chiếu. */
    @Column(name = "duration_minutes", nullable = false)
    private Short durationMinutes;

    /** Nội dung giới thiệu ngắn về phim, hiển thị trên trang danh sách phim. */
    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Short getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Short durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
