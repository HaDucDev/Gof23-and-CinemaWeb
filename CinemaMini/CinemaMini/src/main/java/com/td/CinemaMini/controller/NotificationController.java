package com.td.CinemaMini.controller;

import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.Role;
import com.td.CinemaMini.repository.NotificationRepository;
import com.td.CinemaMini.service.account.AccountService;
import com.td.CinemaMini.service.notification.NotificationService;
import java.security.Principal;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import static com.td.CinemaMini.dto.Values.map;

@RestController
public class NotificationController {
    private final AccountService accounts;
    private final NotificationRepository notifications;
    private final NotificationService service;

    public NotificationController(
            AccountService accounts,
            NotificationRepository notifications,
            NotificationService service) {
        this.accounts = accounts;
        this.notifications = notifications;
        this.service = service;
    }

    /**
     * Trả thông báo của tài khoản đang đăng nhập, dùng cho bảng thông báo trên giao diện.
     *
     * @param principal Thông tin tài khoản đang đăng nhập do Spring Security truyền vào; getName()
     *     trả về email đăng nhập.
     * @return JSON gồm tổng số thông báo chưa đọc và tối đa 50 thông báo mới nhất của tài khoản.
     */
    @GetMapping("/notifications")
    public Map<String, Object> list(Principal principal) {
        UserAccount actor = accounts.account(principal.getName());
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (Notification notification :
                notifications.findTop50ByRecipientIdOrderByCreatedAtDesc(actor.getId())) {
            rows.add(
                    map(
                            "id",
                            notification.getId(),
                            "content",
                            notification.getContent(),
                            "createdAt",
                            notification.getCreatedAt(),
                            "readAt",
                            notification.getReadAt(),
                            "url",
                            (actor.getRole() == Role.ADMIN ? "/admin/bookings/" : "/bookings/")
                                    + notification.getBookingId()));
        }
        return map(
                "unread",
                notifications.countByRecipientIdAndReadAtIsNull(actor.getId()),
                "items",
                rows);
    }

    /**
     * Đánh dấu đã đọc một thông báo sau khi kiểm tra tài khoản hiện tại là người nhận.
     *
     * @param id ID thông báo cần đánh dấu đã đọc từ phần {id} trong URL, tương ứng Notification.id.
     * @param principal Thông tin tài khoản đang đăng nhập do Spring Security truyền vào; getName()
     *     trả về email đăng nhập.
     * @return JSON xác nhận thao tác thành công bằng trường ok.
     */
    @PostMapping("/notifications/{id}/read")
    public Map<String, Object> read(@PathVariable Long id, Principal principal) {
        service.markRead(accounts.account(principal.getName()), id);
        return map("ok", true);
    }
}
