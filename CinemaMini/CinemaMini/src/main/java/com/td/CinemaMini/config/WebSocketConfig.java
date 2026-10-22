package com.td.CinemaMini.config;

import com.td.CinemaMini.service.notification.NotificationSocket;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import java.util.Collections;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final NotificationSocket socket;

    public WebSocketConfig(NotificationSocket socket) {
        this.socket = socket;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Session đăng nhập và kiểm tra same-origin của Spring bảo vệ handshake.
        HttpSessionHandshakeInterceptor interceptor =
                new HttpSessionHandshakeInterceptor(Collections.<String>emptyList());
        interceptor.setCopyHttpSessionId(true);
        registry.addHandler(socket, "/notifications/socket").addInterceptors(interceptor);
    }
}
