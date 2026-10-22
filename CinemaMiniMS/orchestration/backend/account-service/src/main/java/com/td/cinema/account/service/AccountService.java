package com.td.cinema.account.service;

import com.td.cinema.account.dto.request.LoginRequest;
import com.td.cinema.account.dto.request.RegisterRequest;
import com.td.cinema.account.entity.Customer;
import com.td.cinema.account.entity.UserAccount;
import com.td.cinema.account.exception.ApiException;
import com.td.cinema.account.mapper.RepositoryProjectionMapper;
import com.td.cinema.account.model.AccountRole;
import com.td.cinema.account.model.CustomerType;
import com.td.cinema.account.repository.AccountRepository;
import com.td.cinema.account.repository.CustomerRepository;
import com.td.cinema.account.util.Data;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

/** Xử lý tài khoản trong schema Account: đăng ký khách, kiểm tra mật khẩu và cung cấp hồ sơ/quyền. */
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    private final PasswordEncoder passwords;

    /** Nhận repository tài khoản/hồ sơ cùng bộ băm mật khẩu do Spring cung cấp. */
    public AccountService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwords) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.passwords = passwords;
    }

    /**
     * Chuẩn hóa email, kiểm tra trùng, băm mật khẩu rồi lưu tài khoản USER và hồ sơ REGULAR trong
     * cùng transaction.
     */
    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        String email = request.email.trim().toLowerCase(Locale.ROOT);
        if (request.password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw ApiException.bad("Mat khau toi da 72 byte UTF-8.");
        accountRepository.lockOperation("register:" + email);
        if (accountRepository.findByEmail(email).isPresent())
            throw ApiException.conflict("Email da duoc su dung.");
        UserAccount account = new UserAccount();
        account.setEmail(email);
        account.setPasswordHash(passwords.encode(request.password));
        account.setRole(AccountRole.USER);
        account.setActive(true);
        account = accountRepository.saveAndFlush(account);
        long id = account.getAccountId();
        Customer customer = new Customer();
        customer.setAccountId(id);
        customer.setFullName(Data.required(request.fullName, "Ho ten", 120));
        customer.setCustomerType(CustomerType.REGULAR);
        customerRepository.saveAndFlush(customer);
        return get(id);
    }

    /**
     * Đối chiếu BCrypt và chỉ chấp nhận tài khoản đang hoạt động.
     * Tài khoản không tồn tại vẫn dùng hash dự phòng để hạn chế chênh lệch thời gian kiểm tra.
     */
    public Map<String, Object> login(LoginRequest request) {
        UserAccount account =
                accountRepository
                        .findByEmail(request.email.trim().toLowerCase(Locale.ROOT))
                        .orElse(null);
        String hash =
                account == null
                        ? "$2a$10$1fJXSUTB5FpRZPi.00Nw5.hJe2LkD4ZJCDsNRxcGU2vK6bSidRTC."
                        : account.getPasswordHash();
        boolean valid =
                request.password.getBytes(StandardCharsets.UTF_8).length <= 72
                        && passwords.matches(request.password, hash);
        if (!valid || account == null || !Boolean.TRUE.equals(account.getActive()))
            throw new ApiException(401, "INVALID_CREDENTIALS", "Email hoac mat khau khong dung.");
        return get(account.getAccountId());
    }

    /** Đọc hồ sơ của tài khoản đang hoạt động; không tìm thấy thì trả lỗi nghiệp vụ. */
    public Map<String, Object> get(long id) {
        Map<String, Object> profile =
                RepositoryProjectionMapper.required(accountRepository.findActiveProfile(id));
        return profile;
    }

    /** Lấy các tài khoản ADMIN đang hoạt động để các service gửi thông báo đúng người nhận. */
    public List<Map<String, Object>> admins() {
        return RepositoryProjectionMapper.rows(
                accountRepository.findActiveAdmins(AccountRole.ADMIN));
    }
}
