package structural.proxy.problem.model;

import java.util.Objects;

// Đại diện người yêu cầu xem báo cáo, có tên và vai trò để kiểm tra quyền.
public class User {
    private final String name; // Tên người dùng, ví dụ: "Alice".
    private final Role role; // Vai trò dùng để kiểm tra quyền: MANAGER hoặc STAFF.

    public User(String name, Role role) {
        this.name = Objects.requireNonNull(name, "Tên người dùng không được null.");
        this.role = Objects.requireNonNull(role, "Vai trò không được null.");
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }
}
