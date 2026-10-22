package com.td.cinema.account.config;

import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Cấu hình bộ băm mật khẩu BCrypt để Account lưu và đối chiếu mật khẩu an toàn. */
@Configuration
public class PasswordEncoderConfig {
    /** Cung cấp PasswordEncoder dùng BCrypt để băm mật khẩu mới và đối chiếu lúc đăng nhập. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
