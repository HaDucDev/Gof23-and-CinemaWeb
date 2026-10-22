package com.td.cinema.choreography.seat.messaging;

import com.td.cinema.choreography.seat.util.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.function.Function;

/**
 * Ranh giới Local Transaction của một lần xử lý sự kiện trong database của service này.
 * Thay đổi nghiệp vụ, dấu nhận sự kiện và phản hồi lưu đệm cùng commit hoặc cùng rollback.
 */
@Service
public class EventTransactions {
    private static final Logger LOG = LoggerFactory.getLogger(EventTransactions.class);
    private final EventReceiptRepository receipts;
    private final EventPublisher publisher;
    private final ObjectMapper json;
    public EventTransactions(EventReceiptRepository receipts, EventPublisher publisher, ObjectMapper json) {
        this.receipts = receipts; this.publisher = publisher; this.json = json;
    }
    /**
     * Khóa eventId, chống xử lý trùng và chạy nghiệp vụ trong một transaction cục bộ.
     * Các hàm @Transactional của service được gọi bên trong dùng chung transaction này theo
     * propagation REQUIRED mặc định; sự kiện phản hồi chỉ được phát sau commit.
     */
    @Transactional
    public void handle(SagaEvent event, Function<SagaEvent, SagaEvent> action) {
        // Khóa theo eventId trong DB, giữ tới cuối local transaction; không phải khóa queue RabbitMQ.
        receipts.lock(event.eventId.toString());
        String hash = Data.hash(json.writeValueAsString(event));
        List<Object[]> prior = receipts.find(event.eventId.toString());
        SagaEvent reply;
        if (!prior.isEmpty()) {
            // Broker có thể giao lại message: dùng receipt để không chạy thu/hoàn/trả ghế trùng lần nữa.
            // Nếu có phản hồi đã lưu, có thể phát lại cùng phản hồi sau commit để bên nhận tiếp tục.
            if (!hash.equals(prior.get(0)[0])) throw new IllegalArgumentException("Event ID reused with different payload");
            String cached = (String) prior.get(0)[1];
            reply = cached == null || "null".equals(cached) ? null : json.readValue(cached, SagaEvent.class);
        } else {
            // Lần nhận đầu: chạy nghiệp vụ rồi lưu receipt/phản hồi trong cùng transaction với thay đổi DB.
            reply = action.apply(event);
            receipts.insert(event.eventId.toString(), hash, reply == null ? null : json.writeValueAsString(reply));
        }
        // Callback publish phản hồi chạy sau commit, trước khi listener hoàn tất để container ACK.
        // Vì thế message còn Unacked không đủ kết luận transaction SQL chưa commit.
        if (reply != null) publisher.afterCommit(reply);
        // Đây là log handler đã chạy; commit diễn ra sau khi hàm trả về thành công.
        LOG.info("SAGA consumed type={} event={} booking={} duplicate={}", event.type, event.eventId, event.bookingId, !prior.isEmpty());
    }
}
