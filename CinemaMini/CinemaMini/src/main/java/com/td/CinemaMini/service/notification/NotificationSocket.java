package com.td.CinemaMini.service.notification;

import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.context.event.EventListener;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/** Quản lý WebSocket theo tài khoản để đồng bộ danh sách thông báo giữa các tab. */
@Component
public class NotificationSocket extends TextWebSocketHandler {
    // Khóa của Map là email đăng nhập; mỗi tài khoản có thể có nhiều kết nối đang mở.
    private final Map<String, Set<WebSocketSession>> sessions =
            new ConcurrentHashMap<String, Set<WebSocketSession>>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Chỉ chấp nhận kết nối có danh tính đăng nhập do Spring Security xác định.
        if (session.getPrincipal() == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        // Gom kết nối theo email của Principal để gửi đúng tài khoản, không dùng email từ form.
        sessions.computeIfAbsent(
                        session.getPrincipal().getName(),
                        k -> new CopyOnWriteArraySet<WebSocketSession>())
                .add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        if (session.getPrincipal() == null) {
            return;
        }
        // Kết nối đã đóng được gỡ khỏi nhóm để các lần cập nhật sau không gửi vào kết nối cũ.
        Set<WebSocketSession> group = sessions.get(session.getPrincipal().getName());
        if (group != null) {
            group.remove(session);
        }
    }

    public void changed(String email) {
        // Tìm các tab của đúng tài khoản cần làm mới thông báo.
        Set<WebSocketSession> group = sessions.get(email);
        if (group == null) {
            return;
        }
        for (WebSocketSession session : group) {
            try {
                // Tuần tự hóa việc gửi trên cùng kết nối để hai tác vụ không ghi thông điệp đồng
                // thời.
                synchronized (session) {
                    if (session.isOpen()) {
                        // Chỉ gửi tín hiệu refresh; trình duyệt sẽ tải lại nội dung thông báo qua
                        // API.
                        session.sendMessage(new TextMessage("{\"refresh\":true}"));
                    }
                }
            } catch (IOException e) {
                closeSession(session);
                group.remove(session);
            }
        }
    }

    public void logout(String email) {
        // Khi đăng xuất theo email, đóng toàn bộ kết nối WebSocket đang thuộc tài khoản đó.
        Set<WebSocketSession> group = sessions.remove(email);
        if (group != null) {
            for (WebSocketSession session : group) {
                closeSession(session);
            }
        }
    }

    @EventListener
    public void sessionEnded(HttpSessionDestroyedEvent event) {
        // Khi một HTTP session kết thúc, chỉ đóng kết nối gắn với đúng ID session đó.
        for (Set<WebSocketSession> group : sessions.values()) {
            for (WebSocketSession session : group) {
                if (event.getId()
                        .equals(
                                session.getAttributes()
                                        .get(
                                                HttpSessionHandshakeInterceptor
                                                        .HTTP_SESSION_ID_ATTR_NAME))) {
                    closeSession(session);
                    group.remove(session);
                }
            }
        }
    }

    private void closeSession(WebSocketSession session) {
        // Đóng kết nối một cách an toàn; bỏ qua lỗi đóng để tiếp tục dọn các kết nối khác.
        try {
            session.close();
        } catch (IOException ignored) {
        }
    }
}
