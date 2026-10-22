package com.td.cinema.choreography.account.entity;

import com.td.cinema.choreography.account.model.AccountRole;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_accounts", schema = "account")
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long value) {
        this.accountId = value;
    }

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        this.email = value;
    }

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String value) {
        this.passwordHash = value;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 16)
    private AccountRole role = AccountRole.USER;

    public AccountRole getRole() {
        return role;
    }

    public void setRole(AccountRole value) {
        this.role = value;
    }

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean value) {
        this.active = value;
    }

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime value) {
        this.createdAt = value;
    }

    @Column(name = "email_verified_at")
    private OffsetDateTime emailVerifiedAt;

    public OffsetDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(OffsetDateTime value) {
        this.emailVerifiedAt = value;
    }
}
