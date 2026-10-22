package structural.proxy.solution.model;

// Các vai trò người dùng: quản lý được xem báo cáo, nhân viên thường bị từ chối.
public enum Role {
    MANAGER, // Quản lý: được phép xem báo cáo doanh thu.
    STAFF // Nhân viên thường: không được phép xem báo cáo trong demo.
}
