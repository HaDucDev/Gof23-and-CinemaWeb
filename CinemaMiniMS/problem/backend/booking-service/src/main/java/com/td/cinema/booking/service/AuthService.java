package com.td.cinema.booking.service;

import com.td.cinema.booking.client.AccountClient;
import com.td.cinema.booking.dto.request.LoginRequest;
import com.td.cinema.booking.dto.request.RegisterRequest;

import org.springframework.stereotype.Service;

import java.util.*;

/** Chuyển yêu cầu xác thực và hồ sơ sang Account; Booking không trực tiếp đọc database Account. */
@Service
public class AuthService {
    private final AccountClient account;

    /** Nhận client gọi API nội bộ của Account. */
    public AuthService(AccountClient account) {
        this.account = account;
    }

    /** Gọi Account tạo tài khoản; dữ liệu tài khoản được lưu ở schema Account. */
    public Map<String, Object> register(RegisterRequest request) {
        return account.register(request);
    }

    /** Gọi Account xác thực thông tin đăng nhập; việc tạo session do SessionService phụ trách. */
    public Map<String, Object> login(LoginRequest request) {
        return account.login(request);
    }

    /** Lấy hồ sơ đang hoạt động từ Account theo mã tài khoản. */
    public Map<String, Object> profile(long id) {
        return account.profile(id);
    }
}
