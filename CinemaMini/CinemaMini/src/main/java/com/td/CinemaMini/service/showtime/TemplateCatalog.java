package com.td.CinemaMini.service.showtime;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Đọc config/showtime-templates.json một lần khi ứng dụng khởi động và giữ mẫu suất chiếu.
 *
 * <p>Tên mẫu, giá cơ bản và phụ thu VIP được dùng để điền sẵn form tạo suất của admin.
 * Danh sách mã ghế được dùng để hiển thị các ghế có thể chọn không mở bán trong suất mới.
 *
 * <p>Đọc file chỉ lưu mẫu vào bộ nhớ. Suất mới được ghi vào database khi admin gửi form.
 * Xem giải thích từng trường tại src/main/resources/config/README.md.
 */
@Component
public class TemplateCatalog {
    /** Mẫu gốc đã đọc từ JSON; standard() trả bản sao để các nơi sử dụng chỉnh riêng. */
    private final ShowtimeTemplate standard;

    public TemplateCatalog(ObjectMapper mapper) throws IOException {
        // JSON cung cấp giá trị mặc định cho mẫu, không tự tạo suất hay ghế trong database.
        try (InputStream stream =
                new ClassPathResource("config/showtime-templates.json").getInputStream()) {
            // Chuyển các trường name, pricing và seats trong JSON thành đối tượng ShowtimeTemplate.
            standard = mapper.readValue(stream, ShowtimeTemplate.class);
        }
    }

    /** Tạo bản sao gồm giá và từng ghế; sửa bản sao không làm đổi mẫu gốc trong catalog. */
    public ShowtimeTemplate standard() {
        // Mỗi nơi sử dụng nhận một bản sao riêng để điền form hoặc tạo suất mà không sửa mẫu gốc.
        return standard.copy();
    }
}
