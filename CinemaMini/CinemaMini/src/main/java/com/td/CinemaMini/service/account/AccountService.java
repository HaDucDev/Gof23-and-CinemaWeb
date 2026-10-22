package com.td.CinemaMini.service.account;

import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.CustomerType;
import com.td.CinemaMini.model.enums.Role;
import com.td.CinemaMini.repository.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Quản lý đăng ký, tra tài khoản đăng nhập và lấy hồ sơ người mua vé. */
@Service
public class AccountService {
    private final UserAccountRepository accounts;
    private final CustomerRepository customers;
    private final PasswordEncoder passwords;

    public AccountService(
            UserAccountRepository accounts,
            CustomerRepository customers,
            PasswordEncoder passwords) {
        this.accounts = accounts;
        this.customers = customers;
        this.passwords = passwords;
    }

    /** Chuẩn hóa email để đăng ký và đăng nhập cùng tìm đến một tài khoản. */
    public static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    /** Kiểm tra định dạng và giới hạn 254 ký tự của email trước khi lưu tài khoản. */
    public static boolean validEmail(String email) {
        return email != null
                && email.length() <= 254
                && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    @Transactional
    public void register(String name, String email, String password, String confirm) {
        // Dùng email đã bỏ khoảng trắng và đổi thành chữ thường khi kiểm tra trùng.
        email = normalize(email);
        if (name == null || name.trim().isEmpty() || name.trim().length() > 120) {
            throw BusinessException.invalid("Họ tên phải có từ 1 đến 120 ký tự.");
        }
        if (!validEmail(email)) {
            throw BusinessException.invalid("Email không đúng định dạng.");
        }
        // Kiểm tra cả số ký tự lẫn số byte UTF-8: mật khẩu có dấu có thể chiếm nhiều byte.
        // Giới hạn 72 byte phù hợp với cách băm mật khẩu BCrypt đang dùng.
        if (password == null
                || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw BusinessException.invalid("Mật khẩu cần ít nhất 8 ký tự và tối đa 72 byte.");
        }
        if (!password.equals(confirm)) {
            throw BusinessException.invalid("Mật khẩu xác nhận không khớp.");
        }
        // Không cho đăng ký thêm tài khoản với email đã được sử dụng.
        if (accounts.findByEmail(email).isPresent()) {
            throw BusinessException.conflict("Email đã được sử dụng.");
        }
        // Tạo tài khoản có quyền USER; lưu bản băm mật khẩu để dùng khi đăng nhập.
        UserAccount account = new UserAccount();
        account.setEmail(email);
        account.setPasswordHash(passwords.encode(password));
        account.setRole(Role.USER);
        account.setActive(true);
        // Lưu tài khoản để có UserAccount.id, rồi liên kết hồ sơ người mua vé bằng ID này.
        accounts.saveAndFlush(account);
        // Hồ sơ mới thuộc nhóm khách thường; nhóm khách được dùng khi tính ưu đãi vé.
        Customer customer = new Customer();
        customer.setAccountId(account.getId());
        customer.setFullName(name.trim());
        customer.setCustomerType(CustomerType.REGULAR);
        customers.save(customer);
    }

    @Transactional(readOnly = true)
    public UserAccount account(String email) {
        // Tra tài khoản bằng email đăng nhập đã chuẩn hóa.
        // Không tìm thấy tài khoản hoặc tài khoản bị khóa đều bị từ chối sử dụng.
        UserAccount account =
                accounts.findByEmail(normalize(email)).orElseThrow(BusinessException::forbidden);
        if (!account.getActive()) {
            throw BusinessException.forbidden();
        }
        return account;
    }

    @Transactional(readOnly = true)
    public Customer customer(UserAccount account) {
        // Chỉ tài khoản USER mới được dùng hồ sơ khách hàng để mua vé.
        if (account.getRole() != Role.USER) {
            throw BusinessException.forbidden();
        }
        // Tìm bằng UserAccount.id, không dùng Customer.id hay email liên hệ của đơn vé.
        return customers.findByAccountId(account.getId()).orElseThrow(BusinessException::forbidden);
    }

    public void requireAdmin(UserAccount account) {
        // Bảo vệ các chức năng quản lý rạp: tài khoản USER không được thao tác như admin.
        if (account.getRole() != Role.ADMIN) {
            throw BusinessException.forbidden();
        }
    }
}
