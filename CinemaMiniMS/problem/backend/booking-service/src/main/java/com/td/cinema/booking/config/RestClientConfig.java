package com.td.cinema.booking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Arrays;

/** Tạo các HTTP client riêng để Booking gọi Account, Seat và Payment với khóa nội bộ và thời gian chờ. */
@Configuration
public class RestClientConfig {
    private final int connectTimeout;
    private final int readTimeout;
    private final String key;

    /** Nạp thời gian chờ kết nối, thời gian chờ phản hồi và khóa gọi API nội bộ từ cấu hình service. */
    public RestClientConfig(
            @Value("${cinema.http.connect-timeout-ms}") int connectTimeout,
            @Value("${cinema.http.read-timeout-ms}") int readTimeout,
            @Value("${cinema.internal-key}") String key) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.key = key;
    }

    /**
     * Kiểm tra URL gốc hợp lệ rồi tạo RestClient có timeout và header X-Internal-Key cho lời gọi
     * liên service.
     */
    private RestClient client(String url) {
        URI endpoint = URI.create(url);
        if (!Arrays.asList("http", "https").contains(endpoint.getScheme())
                || endpoint.getHost() == null
                || endpoint.getUserInfo() != null
                || endpoint.getQuery() != null
                || endpoint.getFragment() != null)
            throw new IllegalArgumentException("Invalid peer base URL");
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return RestClient.builder()
                .baseUrl(url)
                .defaultHeader("X-Internal-Key", key)
                .requestFactory(factory)
                .build();
    }

    /** Cung cấp HTTP client mang tên accountRestClient để gọi API của Account service. */
    @Bean("accountRestClient")
    public RestClient account(@Value("${cinema.account-url}") String url) {
        return client(url);
    }

    /** Cung cấp HTTP client mang tên seatRestClient để gọi API của Seat service. */
    @Bean("seatRestClient")
    public RestClient seat(@Value("${cinema.seat-url}") String url) {
        return client(url);
    }

    /** Cung cấp HTTP client mang tên paymentRestClient để gọi API của Payment service. */
    @Bean("paymentRestClient")
    public RestClient payment(@Value("${cinema.payment-url}") String url) {
        return client(url);
    }
}
