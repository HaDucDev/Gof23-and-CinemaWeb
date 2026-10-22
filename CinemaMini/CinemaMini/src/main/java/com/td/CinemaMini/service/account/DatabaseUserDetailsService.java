package com.td.CinemaMini.service.account;

import com.td.CinemaMini.model.UserAccount;
import com.td.CinemaMini.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/** Cung cấp thông tin tài khoản trong database cho Spring Security kiểm tra đăng nhập. */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserAccountRepository accounts;

    public DatabaseUserDetailsService(UserAccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        // Username của hệ thống là email; chuẩn hóa giống lúc đăng ký trước khi tra tài khoản.
        UserAccount account =
                accounts.findByEmail(AccountService.normalize(email))
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "Thông tin đăng nhập không đúng."));
        // Đưa bản băm mật khẩu và quyền USER/ADMIN cho Spring Security xử lý xác thực.
        // Tài khoản active = false được đánh dấu disabled để chặn đăng nhập.
        return User.withUsername(account.getEmail())
                .password(account.getPasswordHash())
                .roles(account.getRole().name())
                .disabled(!account.getActive())
                .build();
    }
}
