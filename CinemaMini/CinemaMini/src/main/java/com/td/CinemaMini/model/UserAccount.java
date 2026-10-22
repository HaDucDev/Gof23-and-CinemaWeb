package com.td.CinemaMini.model;

import com.td.CinemaMini.model.enums.Role;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Tài khoản đăng nhập của khách hoặc admin, lưu email, mật khẩu đã băm và quyền truy cập.
 *
 * Các field lưu ID của bảng khác chỉ chứa số ID. Service kiểm tra bản ghi liên quan; class không
 * khai báo quan hệ JPA hoặc tạo khóa ngoại.
 */
@Entity
@Table(name = "user_account")
public class UserAccount {
    /** ID tài khoản trong bảng user_account; khác với Customer.id là ID hồ sơ khách mua vé. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** Email dùng đăng nhập; được lưu dạng chữ thường và bỏ khoảng trắng ở hai đầu. */
    @Column(name = "email", nullable = false, length = 254)
    private String email;

    /** Bản băm BCrypt của mật khẩu, dùng kiểm tra mật khẩu khi đăng nhập. */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** Quyền truy cập của tài khoản: USER là khách mua vé, ADMIN là người quản lý rạp. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "role", nullable = false, length = 16)
    private Role role = Role.USER;

    /** Tài khoản có được sử dụng không: true cho phép đăng nhập, false khóa tài khoản. */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /** Thời điểm tạo tài khoản. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    /**
     * Thời điểm xác minh email, null nếu chưa có thông tin xác minh. Code hiện tại chưa có luồng
     * xác minh và cập nhật field này.
     */
    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(Instant emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }
}
