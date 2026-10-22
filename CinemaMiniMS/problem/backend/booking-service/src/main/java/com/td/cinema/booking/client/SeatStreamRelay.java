package com.td.cinema.booking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.net.*;

@Component
public class SeatStreamRelay {
    private final String base, key;
    private final int connectTimeout, readTimeout;

    public SeatStreamRelay(
            @Value("${cinema.seat-url}") String base,
            @Value("${cinema.internal-key}") String key,
            @Value("${cinema.http.connect-timeout-ms}") int connectTimeout,
            @Value("${cinema.http.stream-read-timeout-ms}") int readTimeout) {
        this.base = base;
        this.key = key;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public StreamingResponseBody stream(String id) {
        return output -> {
            HttpURLConnection connection =
                    (HttpURLConnection)
                            new URL(
                                            base
                                                    + "/internal/showtimes/"
                                                    + URLEncoder.encode(id, "UTF-8")
                                                    + "/seat-events")
                                    .openConnection();
            connection.setRequestProperty("X-Internal-Key", key);
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            try (InputStream input = connection.getInputStream()) {
                byte[] bytes = new byte[4096];
                int count;
                while ((count = input.read(bytes)) != -1) {
                    output.write(bytes, 0, count);
                    output.flush();
                }
            } catch (IOException e) {
                // A disconnected browser closes the peer stream too; normal EventSource reconnects
                // fetch fresh state.
            } finally {
                connection.disconnect();
            }
        };
    }
}
