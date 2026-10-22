package com.td.cinema.booking.config;

import com.td.cinema.booking.websocket.NotificationSocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.*;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.*;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.*;

@Configuration
@EnableWebSocket
public class BookingSecurity implements WebSocketConfigurer, WebMvcConfigurer {
    private final NotificationSocket socket;
    private final String[] origins;

    public BookingSecurity(
            NotificationSocket socket, @Value("${cinema.frontend-origins}") String origins) {
        this.socket = socket;
        this.origins = origins.split(",");
    }

    @Bean
    public HttpSessionEventPublisher sessionEvents() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public CsrfTokenRepository csrfRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    public UserDetailsService noLocalPasswords() {
        return name -> {
            throw new UsernameNotFoundException("Authentication is delegated to Account.");
        };
    }

    @Bean
    public SecurityFilterChain security(HttpSecurity http, CsrfTokenRepository csrf)
            throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfiguration()))
                .csrf(config -> config.csrfTokenRepository(csrf))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/error",
                                                "/internal/**",
                                                "/api/auth/csrf",
                                                "/api/auth/login",
                                                "/api/auth/register")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/movies",
                                                "/api/showtimes",
                                                "/api/showtimes/**")
                                        .permitAll()
                                        .requestMatchers("/api/admin/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers("/api/holds/**", "/api/bookings/**")
                                        .hasRole("USER")
                                        .anyRequest()
                                        .authenticated())
                .formLogin(config -> config.disable())
                .httpBasic(config -> config.disable())
                .logout(config -> config.disable())
                .exceptionHandling(
                        errors ->
                                errors.authenticationEntryPoint(
                                                (request, response, error) -> {
                                                    response.setStatus(401);
                                                    response.setContentType("application/json");
                                                    response.getWriter()
                                                            .write(
                                                                    "{\"code\":\"LOGIN_REQUIRED\",\"message\":\"Vui"
                                                                        + " long dang nhap.\"}");
                                                })
                                        .accessDeniedHandler(
                                                (request, response, error) -> {
                                                    response.setStatus(403);
                                                    response.setContentType("application/json");
                                                    response.getWriter()
                                                            .write(
                                                                    "{\"code\":\"FORBIDDEN\",\"message\":\"Khong"
                                                                        + " du quyen hoac CSRF"
                                                                        + " token khong hop"
                                                                        + " le.\"}");
                                                }));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfiguration() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(Arrays.asList(origins));
        cors.setAllowCredentials(true);
        cors.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(Arrays.asList("Content-Type", "X-CSRF-TOKEN", "Idempotency-Key"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        HttpSessionHandshakeInterceptor interceptor = new HttpSessionHandshakeInterceptor();
        interceptor.setCopyAllAttributes(false);
        interceptor.setCopyHttpSessionId(true);
        interceptor.setCreateSession(false);
        registry.addHandler(socket, "/ws/notifications")
                .addInterceptors(interceptor)
                .setAllowedOrigins(origins);
    }

    @Bean(name = "sagaExecutor")
    public ThreadPoolTaskExecutor sagaExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("saga-rest-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(15);
        return executor;
    }

    @Bean(name = "streamExecutor")
    public ThreadPoolTaskExecutor streamExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("seat-stream-");
        return executor;
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer config) {
        config.setDefaultTimeout(1200000L);
        config.setTaskExecutor(streamExecutor());
    }
}
