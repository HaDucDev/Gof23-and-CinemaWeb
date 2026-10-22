package com.td.cinema.choreography.payment.messaging;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Receipt trong database riêng của payment-service để chống xử lý trùng theo eventId.
 * Đây là dấu nhận nghiệp vụ trong SQL, không phải ACK của RabbitMQ và cũng không phải durable outbox.
 * Các thao tác được gọi trong local transaction của EventTransactions.
 */
@Repository
public class EventReceiptRepository {
    @PersistenceContext private EntityManager entityManager;
    /** Khóa transaction PostgreSQL để hai lần giao cùng eventId không chạy nghiệp vụ đồng thời. */
    public void lock(String eventId) {
        entityManager.createNativeQuery("select 1 from pg_advisory_xact_lock(hashtextextended(:key,0))")
                .setParameter("key", "event:" + eventId).getSingleResult();
    }
    /** Tìm hash body và phản hồi đã lưu để kiểm tra message trùng và dùng lại kết quả. */
    @SuppressWarnings("unchecked")
    public List<Object[]> find(String id) {
        return entityManager.createNativeQuery("select payload_hash, cast(response as text) from payment.received_events where event_id=cast(:id as uuid)")
                .setParameter("id", id).getResultList();
    }
    /** Lưu receipt/phản hồi cùng transaction nghiệp vụ; rollback nghiệp vụ thì receipt cũng rollback. */
    public void insert(String id, String hash, String response) {
        entityManager.createNativeQuery("insert into payment.received_events(event_id,payload_hash,response) values(cast(:id as uuid),:hash,cast(:response as jsonb))")
                .setParameter("id", id).setParameter("hash", hash).setParameter("response", response).executeUpdate();
    }
}
