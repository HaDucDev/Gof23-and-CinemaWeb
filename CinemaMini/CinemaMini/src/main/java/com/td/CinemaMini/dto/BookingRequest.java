package com.td.CinemaMini.dto;

import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.service.account.AccountService;
import java.util.*;

/** GoF: Creational / Builder. Chốt yêu cầu bất biến; service kiểm tra quyền và dữ liệu DB. */
public final class BookingRequest {
    private final Long customerId;
    private final Long showtimeId;
    private final Long holdId;
    private final UUID holdToken;
    private final long revision;
    private final List<Long> seatIds;
    private final String contactName;
    private final String contactEmail;
    private final String contactPhone;
    private final String note;

    private BookingRequest(Builder b) {
        if (b.customerId == null
                || b.showtimeId == null
                || b.holdId == null
                || b.holdToken == null) {
            throw BusinessException.invalid("Thiếu thông tin phiên giữ ghế.");
        }
        if (b.seatIds == null
                || b.seatIds.isEmpty()
                || b.seatIds.size() > 6
                || b.seatIds.contains(null)
                || new HashSet<Long>(b.seatIds).size() != b.seatIds.size()) {
            throw BusinessException.invalid("Cần chọn từ 1 đến 6 ghế khác nhau.");
        }
        String name = b.contactName == null ? "" : b.contactName.trim();
        String email = AccountService.normalize(b.contactEmail);
        if (name.isEmpty() || name.length() > 120 || !AccountService.validEmail(email)) {
            throw BusinessException.invalid("Tên hoặc email liên hệ không hợp lệ.");
        }
        if ((b.contactPhone != null && b.contactPhone.length() > 30)
                || (b.note != null && b.note.length() > 1000)) {
            throw BusinessException.invalid("Số điện thoại hoặc ghi chú quá dài.");
        }
        customerId = b.customerId;
        showtimeId = b.showtimeId;
        holdId = b.holdId;
        holdToken = b.holdToken;
        revision = b.revision;
        List<Long> copy = new ArrayList<Long>(b.seatIds);
        Collections.sort(copy);
        seatIds = Collections.unmodifiableList(copy);
        contactName = name;
        contactEmail = email;
        contactPhone = b.contactPhone;
        note = b.note;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public Long getHoldId() {
        return holdId;
    }

    public UUID getHoldToken() {
        return holdToken;
    }

    public long getRevision() {
        return revision;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getNote() {
        return note;
    }

    public String fingerprint() {
        // Phân cách kèm độ dài tránh nhập liệu chứa ký tự phân cách tạo cùng payload.
        List<String> values =
                Arrays.asList(
                        String.valueOf(holdId),
                        holdToken.toString(),
                        String.valueOf(showtimeId),
                        String.valueOf(revision),
                        seatIds.toString(),
                        contactName,
                        contactEmail,
                        contactPhone == null ? "" : contactPhone,
                        note == null ? "" : note);
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            result.append(value.length()).append(':').append(value);
        }
        return result.toString();
    }

    public static final class Builder {
        private Long customerId;
        private Long showtimeId;
        private Long holdId;
        private UUID holdToken;
        private long revision;
        private List<Long> seatIds;
        private String contactName;
        private String contactEmail;
        private String contactPhone;
        private String note;

        public Builder customerId(Long v) {
            customerId = v;
            return this;
        }

        public Builder showtimeId(Long v) {
            showtimeId = v;
            return this;
        }

        public Builder holdId(Long v) {
            holdId = v;
            return this;
        }

        public Builder holdToken(UUID v) {
            holdToken = v;
            return this;
        }

        public Builder revision(long v) {
            revision = v;
            return this;
        }

        public Builder seatIds(List<Long> v) {
            seatIds = v;
            return this;
        }

        public Builder contactName(String v) {
            contactName = v;
            return this;
        }

        public Builder contactEmail(String v) {
            contactEmail = v;
            return this;
        }

        public Builder contactPhone(String v) {
            contactPhone = v;
            return this;
        }

        public Builder note(String v) {
            note = v;
            return this;
        }

        public BookingRequest build() {
            return new BookingRequest(this);
        }
    }
}
