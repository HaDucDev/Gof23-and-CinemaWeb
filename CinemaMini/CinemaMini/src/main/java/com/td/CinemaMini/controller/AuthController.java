package com.td.CinemaMini.controller;

import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.service.account.AccountService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    private final AccountService accounts;

    public AuthController(AccountService accounts) {
        this.accounts = accounts;
    }

    /**
     * Hiển thị form đăng nhập. Việc nhận email và mật khẩu qua POST /login do Spring Security xử
     * lý.
     *
     * @return Tên trang login.
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Hiển thị form đăng ký tài khoản khách hàng.
     *
     * @return Tên trang register.
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    /**
     * Nhận form đăng ký, tạo tài khoản USER và hồ sơ khách tương ứng.
     *
     * @param fullName Họ tên từ form đăng ký, được lưu vào hồ sơ Customer của tài khoản mới.
     * @param email Email từ form đăng ký, dùng làm email đăng nhập; service chuẩn hóa trước khi
     *     lưu.
     * @param password Mật khẩu người dùng nhập từ form; service băm BCrypt trước khi lưu vào tài
     *     khoản.
     * @param confirmPassword Mật khẩu nhập lại từ form, phải trùng với password.
     * @param model Model do Spring cấp để trả thông báo lỗi, họ tên và email về form nếu đăng ký
     *     không thành công.
     * @return Chuyển sang trang đăng nhập khi thành công; hiển thị lại trang register nếu dữ liệu
     *     bị từ chối.
     */
    @PostMapping("/register")
    public String create(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {
        try {
            accounts.register(fullName, email, password, confirmPassword);
            return "redirect:/login?registered";
        } catch (BusinessException error) {
            model.addAttribute("message", error.getMessage());
        } catch (DataIntegrityViolationException error) {
            model.addAttribute("message", "Email đã được sử dụng.");
        }
        model.addAttribute("fullName", fullName);
        model.addAttribute("email", email);
        return "register";
    }
}
