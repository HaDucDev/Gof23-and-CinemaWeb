package com.td.CinemaMini.model;

import com.td.CinemaMini.model.enums.CustomerType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Hồ sơ người mua vé, gắn với một tài khoản và có nhóm khách để áp dụng ưu đãi.
 *
 * Các field lưu ID của bảng khác chỉ chứa số ID. Service kiểm tra bản ghi liên quan; class không
 * khai báo quan hệ JPA hoặc tạo khóa ngoại.
 */
@Entity
@Table(name = "customer")
public class Customer {
    /** ID hồ sơ khách trong bảng customer; khác với ID tài khoản đăng nhập. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** ID tài khoản đăng nhập của khách này, tương ứng UserAccount.id. */
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /**
     * Họ tên khách lưu trong hồ sơ khi đăng ký; thông tin liên hệ trên từng đơn được nhập riêng.
     */
    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    /** Nhóm khách dùng để chọn mức giảm giá: REGULAR, STUDENT hoặc MEMBER. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "customer_type", nullable = false, length = 16)
    private CustomerType customerType = CustomerType.REGULAR;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType;
    }
}
