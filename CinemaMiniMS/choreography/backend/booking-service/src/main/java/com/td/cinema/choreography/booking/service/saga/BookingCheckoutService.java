package com.td.cinema.choreography.booking.service.saga;
import com.td.cinema.choreography.booking.client.*;
import com.td.cinema.choreography.booking.dto.request.*;
import com.td.cinema.choreography.booking.util.Data;
import org.springframework.stereotype.Service;
import java.util.*;
/**
 * Chuẩn bị đơn qua REST đến bước checkout; thanh toán/hủy được phát thành sự kiện sau transaction
 * Booking.
 */
@Service
public class BookingCheckoutService {
    private final BookingTransactionService store;
    private final AccountClient accounts;
    private final SeatClient seats;
    /** Nhận nơi lưu đơn cùng client Account và Seat cho giai đoạn chuẩn bị checkout. */
    public BookingCheckoutService(BookingTransactionService store, AccountClient accounts, SeatClient seats) {
        this.store=store; this.accounts=accounts; this.seats=seats;
    }
    /** Lấy hồ sơ khách và phiên giữ ghế, tạo đơn CREATING rồi checkout với Seat để đưa đơn sang PENDING. */
    public UUID create(long actor,String key,BookingCreateRequest request) {
        UUID id=store.beginCreate(actor,key,request,accounts.profile(actor),seats.hold(request.holdId,actor));
        if ("CREATING".equals(Data.text(store.raw(id),"status"))) {
            HoldCommandRequest checkout=new HoldCommandRequest();
            checkout.bookingId=id; checkout.accountId=actor; checkout.token=request.token; checkout.revision=request.revision;
            store.checkout(id,Data.number(seats.checkout(request.holdId,checkout),"revision"));
        }
        return id;
    }
    /** Lưu yêu cầu Pay/hủy và phát sự kiện sau commit; việc thanh toán, trả ghế được các consumer xử lý. */
    public void start(UUID id,long actor,String key,String kind,String detail) { store.enqueue(id,actor,key,kind,detail); }
}
