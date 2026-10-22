package com.td.CinemaMini.service.booking;

import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.service.booking.exception.StaleHoldException;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Điều phối giữ/trả ghế và thử lại sau khi dọn phiên giữ ghế cũ đã quá hạn. */
@Service
public class SeatHoldService {
    private final HoldMutationService mutations;
    private final HoldExpirationService expiration;

    public SeatHoldService(HoldMutationService mutations, HoldExpirationService expiration) {
        this.mutations = mutations;
        this.expiration = expiration;
    }

    public HoldSession add(
            UserAccount actor,
            Customer customer,
            Long showtimeId,
            Long holdId,
            UUID token,
            Long seatId,
            long revision,
            String key) {
        // Thử tối đa hai lần: lần đầu có thể gặp ghế của một phiên khác đã hết hạn.
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                return mutations.add(
                        actor, customer, showtimeId, holdId, token, seatId, revision, key);
            } catch (StaleHoldException stale) {
                // Transaction giữ mới đã rollback/nhả khóa ghế; dọn phiên cũ theo đúng thứ tự.
                // Khi đã nhả khóa của lần thử cũ, dọn phiên quá hạn trong transaction riêng rồi thử
                // lại.
                expiration.expire(stale.getHoldId());
            }
        }
        // Hai lần vẫn chưa giữ được ghế thì báo xung đột để người dùng đồng bộ và chọn lại.
        throw new BusinessException(
                409, "SEAT_UNAVAILABLE", "Ghế đang thay đổi. Vui lòng thử lại.");
    }

    public HoldSession remove(
            UserAccount actor,
            Customer customer,
            Long holdId,
            UUID token,
            Long seatId,
            long revision,
            String key) {
        // Chuyển yêu cầu trả ghế sang service có transaction để kiểm tra token, revision và trả
        // chỗ.
        return mutations.remove(actor, customer, holdId, token, seatId, revision, key);
    }
}
