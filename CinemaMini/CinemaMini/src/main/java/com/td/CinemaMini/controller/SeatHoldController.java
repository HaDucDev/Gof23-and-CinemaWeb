package com.td.CinemaMini.controller;

import com.td.CinemaMini.model.*;
import com.td.CinemaMini.service.account.AccountService;
import com.td.CinemaMini.service.booking.*;
import com.td.CinemaMini.dto.PriceBreakdown;
import java.security.Principal;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class SeatHoldController {
    private final AccountService accounts;
    private final SeatHoldService holds;
    private final AvailabilityService availability;

    public SeatHoldController(
            AccountService accounts, SeatHoldService holds, AvailabilityService availability) {
        this.accounts = accounts;
        this.holds = holds;
        this.availability = availability;
    }

    /**
     * Trả trạng thái hiện tại của các ghế trong một suất để vẽ sơ đồ ghế.
     *
     * @param id ID suất chiếu từ phần {id} trong URL, tương ứng Showtime.id.
     * @return JSON gồm ghế, trạng thái, phiên bản, giá từng ghế trước ưu đãi và thời gian của
     *     server/suất chiếu.
     */
    @GetMapping("/showtimes/{id}/availability")
    public Map<String, Object> snapshot(@PathVariable Long id) {
        return availability.snapshot(id);
    }

    /**
     * Tìm phiên giữ ghế gần nhất của khách trong một suất và đồng bộ tình trạng hết hạn nếu cần.
     *
     * @param id ID suất chiếu cần tìm phiên từ phần {id} trong URL, tương ứng Showtime.id.
     * @param principal Thông tin tài khoản đang đăng nhập do Spring Security truyền vào; getName()
     *     trả về email đăng nhập.
     * @return JSON chứa phiên tìm được; trường hold là null nếu không có phiên phù hợp.
     */
    @GetMapping("/showtimes/{id}/my-hold")
    public Map<String, Object> mine(@PathVariable Long id, Principal principal) {
        UserAccount actor = accounts.account(principal.getName());
        return availability.myHold(id, accounts.customer(actor));
    }

    /**
     * Trả thông tin một phiên giữ ghế sau khi kiểm tra phiên thuộc khách đang đăng nhập.
     *
     * @param id ID phiên giữ ghế từ phần {id} trong URL, tương ứng HoldSession.id.
     * @param principal Thông tin tài khoản đang đăng nhập do Spring Security truyền vào; getName()
     *     trả về email đăng nhập.
     * @return JSON thông tin phiên, các ghế còn đang giữ, giá tạm tính và hạn giữ ghế.
     */
    @GetMapping("/holds/{id}")
    public Map<String, Object> hold(@PathVariable Long id, Principal principal) {
        UserAccount actor = accounts.account(principal.getName());
        return availability.holdView(id, accounts.customer(actor));
    }

    /**
     * Tạo phiên giữ ghế mới và giữ ghế đầu tiên mà khách chọn trong suất.
     *
     * @param id ID suất chiếu từ phần {id} trong URL, tương ứng Showtime.id.
     * @param command JSON từ trình duyệt, dùng seatId là ID ghế cần giữ và requestKey là mã lần
     *     thao tác; token và revision chưa dùng ở lần tạo phiên.
     * @param principal Thông tin tài khoản đang đăng nhập do Spring Security truyền vào; getName()
     *     trả về email đăng nhập.
     * @return JSON phiên vừa tạo, gồm ID, token, revision, các ghế đang giữ và hạn giữ ghế.
     */
    @PostMapping("/showtimes/{id}/holds")
    public Map<String, Object> first(
            @PathVariable Long id, @RequestBody HoldCommand command, Principal principal) {
        UserAccount actor = accounts.account(principal.getName());
        Customer customer = accounts.customer(actor);
        HoldSession hold =
                holds.add(actor, customer, id, null, null, command.seatId, 0, command.requestKey);
        return availability.holdView(hold.getId(), customer);
    }

    /**
     * Giữ thêm một ghế vào phiên hiện có của khách.
     *
     * @param id ID phiên giữ ghế từ phần {id} trong URL, tương ứng HoldSession.id.
     * @param command JSON chứa seatId là Seat.id cần giữ, token của phiên, revision mới nhất mà
     *     trình duyệt biết và requestKey của lần thêm ghế.
     * @param principal Thông tin tài khoản đang đăng nhập do Spring Security truyền vào; getName()
     *     trả về email đăng nhập.
     * @return JSON thông tin phiên sau khi xử lý thêm ghế.
     */
    @PostMapping("/holds/{id}/seats")
    public Map<String, Object> add(
            @PathVariable Long id, @RequestBody HoldCommand command, Principal principal) {
        UserAccount actor = accounts.account(principal.getName());
        Customer customer = accounts.customer(actor);
        Map<String, Object> own = availability.holdView(id, customer);
        HoldSession hold =
                holds.add(
                        actor,
                        customer,
                        (Long) own.get("showtimeId"),
                        id,
                        command.token,
                        command.seatId,
                        command.revision,
                        command.requestKey);
        return availability.holdView(hold.getId(), customer);
    }

    /**
     * Trả lại một ghế đang giữ để ghế có thể được khách khác chọn.
     *
     * @param id ID phiên giữ ghế từ phần {id} trong URL, tương ứng HoldSession.id.
     * @param seatId ID ghế cần trả từ phần {seatId} trong URL, tương ứng Seat.id; không dùng seatId
     *     trong JSON ở API này.
     * @param command JSON chứa token của phiên, revision mới nhất mà trình duyệt biết và requestKey
     *     của lần trả ghế.
     * @param principal Thông tin tài khoản đang đăng nhập do Spring Security truyền vào; getName()
     *     trả về email đăng nhập.
     * @return JSON phiên sau khi trả ghế; phiên chuyển sang RELEASED nếu không còn ghế đang giữ.
     */
    @DeleteMapping("/holds/{id}/seats/{seatId}")
    public Map<String, Object> remove(
            @PathVariable Long id,
            @PathVariable Long seatId,
            @RequestBody HoldCommand command,
            Principal principal) {
        UserAccount actor = accounts.account(principal.getName());
        Customer customer = accounts.customer(actor);
        HoldSession hold =
                holds.remove(
                        actor,
                        customer,
                        id,
                        command.token,
                        seatId,
                        command.revision,
                        command.requestKey);
        return availability.holdView(hold.getId(), customer);
    }

    /**
     * Trả toàn bộ ghế của phiên đang chọn và kết thúc phiên đó.
     *
     * @param id ID phiên giữ ghế từ phần {id} trong URL, tương ứng HoldSession.id.
     * @param command JSON chứa token, revision và requestKey của lần trả toàn bộ ghế; không cần
     *     seatId.
     * @param principal Thông tin tài khoản đang đăng nhập do Spring Security truyền vào; getName()
     *     trả về email đăng nhập.
     * @return JSON thông tin phiên sau khi xử lý trả toàn bộ ghế.
     */
    @PostMapping("/holds/{id}/release")
    public Map<String, Object> release(
            @PathVariable Long id, @RequestBody HoldCommand command, Principal principal) {
        UserAccount actor = accounts.account(principal.getName());
        Customer customer = accounts.customer(actor);
        HoldSession hold =
                holds.remove(
                        actor,
                        customer,
                        id,
                        command.token,
                        null,
                        command.revision,
                        command.requestKey);
        return availability.holdView(hold.getId(), customer);
    }

    /**
     * Tính giá xem trước theo các ghế và nhóm khách hiện tại; thao tác này chưa giữ ghế hoặc tạo
     * đơn.
     *
     * @param id ID suất chiếu từ phần {id} trong URL, tương ứng Showtime.id.
     * @param ids Mảng JSON gồm 1-6 giá trị Seat.id khác nhau, ví dụ [1, 2]; đây là toàn bộ body,
     *     không phải một object HoldCommand.
     * @param principal Thông tin tài khoản đang đăng nhập do Spring Security truyền vào; getName()
     *     trả về email đăng nhập.
     * @return JSON PriceBreakdown gồm tiền trước giảm, ưu đãi và tổng tiền dự kiến.
     */
    @PostMapping("/showtimes/{id}/quote")
    public PriceBreakdown quote(
            @PathVariable Long id, @RequestBody List<Long> ids, Principal principal) {
        UserAccount actor = accounts.account(principal.getName());
        return availability.quote(id, accounts.customer(actor), ids);
    }

    /** JSON giữ/trả ghế; các field cần gửi cho từng API được giải thích trong comment trước hàm. */
    public static class HoldCommand {
        /**
         * ID ghế Seat.id cần giữ, dùng khi tạo phiên hoặc thêm ghế. Khi trả một ghế, ID lấy từ URL.
         */
        public Long seatId;
        /**
         * Mã UUID của phiên giữ ghế; cần gửi khi thao tác với phiên đã có.
         */
        public UUID token;
        /**
         * Phiên bản mới nhất của phiên mà trình duyệt biết; server dùng để phát hiện dữ liệu cũ.
         */
        public long revision;
        /**
         * Mã của lần giữ hoặc trả ghế; giữ nguyên mã nếu gửi lại cùng thao tác.
         */
        public String requestKey;
    }
}
