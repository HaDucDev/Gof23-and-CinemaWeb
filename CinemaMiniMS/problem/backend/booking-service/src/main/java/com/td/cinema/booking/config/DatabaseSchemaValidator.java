package com.td.cinema.booking.config;

import com.td.cinema.booking.repository.DatabaseRepository;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Kiểm tra schema, vai trò và quyền truy cập database khi khởi động trước khi báo service sẵn sàng. */
@Component
public class DatabaseSchemaValidator implements ApplicationRunner {
    private final DatabaseRepository repository;
    private volatile boolean ready;

    /** Nhận repository kiểm tra database của chính service này. */
    public DatabaseSchemaValidator(DatabaseRepository repository) {
        this.repository = repository;
    }

    /**
     * Chạy sau khi Spring khởi tạo: xác nhận schema và quyền database đúng service rồi mới đánh dấu
     * sẵn sàng.
     * Nếu kiểm tra thất bại, exception làm quá trình khởi động thất bại.
     */
    @Override
    public void run(ApplicationArguments args) {
        repository.validateSchema();
        ready = true;
    }

    /** Cho HealthService biết việc kiểm tra database lúc khởi động đã hoàn tất thành công hay chưa. */
    public boolean ready() {
        return ready;
    }
}
