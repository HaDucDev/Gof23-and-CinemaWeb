package com.td.cinema.choreography.booking.service;

import com.td.cinema.choreography.booking.util.Data;
import com.td.cinema.choreography.booking.websocket.NotificationSocket;

import jakarta.servlet.http.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.*;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.*;
import org.springframework.stereotype.Service;

import java.util.*;

/** Quản lý phiên đăng nhập ở Booking, SecurityContext, CSRF và các kết nối thông báo gắn với phiên. */
@Service
public class SessionService {
    private final CsrfTokenRepository csrf;
    private final NotificationSocket socket;

    /** Nhận nơi lưu token CSRF và bộ quản lý các kết nối thông báo theo session. */
    public SessionService(CsrfTokenRepository csrf, NotificationSocket socket) {
        this.csrf = csrf;
        this.socket = socket;
    }

    /**
     * Đổi sessionId sau đăng nhập, đóng kênh thông báo cũ và lưu quyền tài khoản vào
     * SecurityContext của phiên.
     * Xóa token CSRF cũ để frontend lấy token phù hợp với phiên mới.
     */
    public void establish(
            Map<String, Object> account, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        socket.closeSession(session.getId());
        request.changeSessionId();
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        String.valueOf(Data.number(account, "accountId")),
                        null,
                        Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + Data.text(account, "role"))));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);
        csrf.saveToken(null, request, response);
    }

    /**
     * Đóng WebSocket của phiên, hủy session, xóa SecurityContext, token CSRF và cookie phiên của
     * bản demo.
     */
    public Map<String, Object> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            socket.closeSession(session.getId());
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        csrf.saveToken(null, request, response);
        Cookie cookie = new Cookie("CINEMA_CHOREO_SESSION", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return Data.map("loggedOut", true);
    }
}
