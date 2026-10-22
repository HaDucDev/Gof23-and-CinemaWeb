package com.td.cinema.choreography.booking.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.*;

@Component
public class NotificationSocket extends TextWebSocketHandler {
    private final ConcurrentMap<String, WebSocketSession> sessions =
            new ConcurrentHashMap<String, WebSocketSession>();
    private final ObjectMapper json;

    public NotificationSocket(ObjectMapper json) {
        this.json = json;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (session.getPrincipal() == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        sessions.put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable error) throws Exception {
        sessions.remove(session.getId());
        session.close();
    }

    public void publish(long actor, Map<String, Object> message) {
        String payload = json.writeValueAsString(message);
        for (WebSocketSession session : sessions.values())
            if (session.getPrincipal() != null
                    && session.getPrincipal().getName().equals(String.valueOf(actor)))
                try {
                    synchronized (session) {
                        if (session.isOpen()) session.sendMessage(new TextMessage(payload));
                    }
                } catch (Exception e) {
                    sessions.remove(session.getId());
                    try {
                        session.close();
                    } catch (Exception ignored) {
                    }
                }
    }

    public void closeSession(String id) {
        for (WebSocketSession session : sessions.values())
            if (id.equals(
                    session.getAttributes()
                            .get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME))) {
                sessions.remove(session.getId());
                try {
                    session.close(CloseStatus.NORMAL);
                } catch (Exception ignored) {
                }
            }
    }

    @EventListener
    public void expired(SessionDestroyedEvent event) {
        closeSession(event.getId());
    }
}
