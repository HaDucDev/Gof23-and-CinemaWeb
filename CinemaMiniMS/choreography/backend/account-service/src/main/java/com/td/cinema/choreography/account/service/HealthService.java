package com.td.cinema.choreography.account.service;

import com.td.cinema.choreography.account.config.DatabaseSchemaValidator;
import com.td.cinema.choreography.account.exception.ApiException;
import com.td.cinema.choreography.account.repository.DatabaseRepository;
import com.td.cinema.choreography.account.util.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/** Kết hợp kết quả kiểm tra schema lúc khởi động với phép kiểm tra kết nối database hiện tại. */
@Service
public class HealthService {
    private final DatabaseRepository repository;
    private final DatabaseSchemaValidator guard;
    private final String name;

    /** Nhận repository kiểm tra kết nối, bộ kiểm tra schema và tên của service để trả trạng thái sẵn sàng. */
    public HealthService(
            DatabaseRepository repository,
            DatabaseSchemaValidator guard,
            @Value("${spring.application.name}") String name) {
        this.repository = repository;
        this.guard = guard;
        this.name = name;
    }

    /** Chỉ trả UP sau khi kiểm tra schema hoàn tất và truy vấn kiểm tra kết nối database thành công. */
    public Map<String, Object> status() {
        if (!guard.ready())
            throw new ApiException(503, "STARTING", "Schema validation is not complete.");
        repository.checkConnection();
        return Data.map("service", name, "status", "UP");
    }
}
