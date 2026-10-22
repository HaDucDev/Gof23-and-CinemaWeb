package com.td.CinemaMini.service.booking;

import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.model.IdempotencyRequest;
import com.td.CinemaMini.repository.IdempotencyRequestRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** Nhận diện yêu cầu gửi lại để không giữ ghế, tạo đơn hoặc ghi giao dịch nhiều lần. */
@Service
public class RequestService {
    private final IdempotencyRequestRepository requests;

    public RequestService(IdempotencyRequestRepository requests) {
        this.requests = requests;
    }

    // Khóa yêu cầu trong cùng transaction với nghiệp vụ để kết quả được lưu hoặc hủy cùng nhau.
    @Transactional(propagation = Propagation.MANDATORY)
    public IdempotencyRequest lock(Long actorId, String key, String operation, String payload) {
        if (key == null || !key.matches("[a-zA-Z0-9_-]{16,128}")) {
            throw BusinessException.invalid("Khóa yêu cầu không hợp lệ. Vui lòng tải lại trang.");
        }
        // Băm loại thao tác và nội dung để phát hiện cùng mã yêu cầu bị dùng với dữ liệu khác.
        String fingerprint = hash(operation + "\n" + payload);
        // Khóa chống lặp luôn đứng trước khóa phiên/đơn/ghế.
        // Tạo bản ghi nếu chưa có; khóa theo UserAccount.id và key để xử lý lần lượt các bản gửi
        // lại.
        requests.insertIfMissing(actorId, key, operation, fingerprint);
        IdempotencyRequest request =
                requests.lockRequest(actorId, key).orElseThrow(BusinessException::missing);
        // Cùng tài khoản và key chỉ được dùng lại đúng thao tác, đúng nội dung đã gửi trước đó.
        if (!operation.equals(request.getOperationType())
                || !fingerprint.equals(request.getPayloadFingerprint())) {
            throw new BusinessException(
                    409, "IDEMPOTENCY_CONFLICT", "Khóa yêu cầu đã dùng với nội dung khác.");
        }
        return request;
    }

    public void complete(IdempotencyRequest request, String type, Long id) {
        // Lưu loại và ID kết quả để lần gửi lại trả đúng phiên hoặc đơn đã được tạo.
        request.setReferenceType(type);
        request.setReferenceId(id);
        request.setResponseStatus((short) 200);
    }

    private String hash(String text) {
        try {
            // SHA-256 ở đây dùng so sánh nội dung yêu cầu, không phải hàm băm mật khẩu đăng nhập.
            byte[] bytes =
                    MessageDigest.getInstance("SHA-256")
                            .digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            // Biểu diễn mỗi byte bằng hai chữ số hệ 16 để có chuỗi dấu vân tay cố định.
            for (byte b : bytes) {
                result.append(String.format("%02x", b & 255));
            }
            return result.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
