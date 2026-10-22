package com.td.CinemaMini.config;

import com.td.CinemaMini.service.notification.NotificationSocket;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class SecurityConfig {
    @Bean
    public HttpSessionEventPublisher sessionEvents() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService details, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(details);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain security(HttpSecurity http, NotificationSocket socket)
            throws Exception {
        http.authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/",
                                                "/movies",
                                                "/login",
                                                "/register",
                                                "/error",
                                                "/css/**",
                                                "/js/**",
                                                "/vendor/**")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/showtimes/*/seats",
                                                "/showtimes/*/availability",
                                                "/showtimes/*/seat-events")
                                        .permitAll()
                                        .requestMatchers("/admin/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers("/notifications/**")
                                        .authenticated()
                                        .anyRequest()
                                        .hasRole("USER"))
                .formLogin(
                        form ->
                                form.loginPage("/login")
                                        .usernameParameter("email")
                                        .successHandler(
                                                (request, response, authentication) -> {
                                                    boolean admin =
                                                            authentication.getAuthorities().stream()
                                                                    .anyMatch(
                                                                            a ->
                                                                                    a.getAuthority()
                                                                                            .equals(
                                                                                                    "ROLE_ADMIN"));
                                                    if (admin) {
                                                        response.sendRedirect(
                                                                request.getContextPath()
                                                                        + "/admin/showtimes");
                                                    } else {
                                                        SavedRequestAwareAuthenticationSuccessHandler
                                                                handler =
                                                                        new SavedRequestAwareAuthenticationSuccessHandler();
                                                        handler.setDefaultTargetUrl("/movies");
                                                        handler.onAuthenticationSuccess(
                                                                request, response, authentication);
                                                    }
                                                })
                                        .permitAll())
                .logout(
                        logout ->
                                logout.addLogoutHandler(
                                                (request, response, auth) -> {
                                                    if (auth != null) {
                                                        socket.logout(auth.getName());
                                                    }
                                                })
                                        .logoutSuccessUrl("/login?logout"))
                .exceptionHandling(
                        errors ->
                                errors.authenticationEntryPoint(
                                                (request, response, error) -> {
                                                    if (String.valueOf(request.getHeader("Accept"))
                                                            .contains("application/json")) {
                                                        response.setStatus(401);
                                                        response.setContentType(
                                                                "application/json;charset=UTF-8");
                                                        response.getWriter()
                                                                .write(
                                                                        "{\"code\":\"LOGIN_REQUIRED\",\"message\":\"Vui lòng đăng nhập để tiếp tục.\"}");
                                                    } else {
                                                        response.sendRedirect(
                                                                request.getContextPath()
                                                                        + "/login");
                                                    }
                                                })
                                        .accessDeniedHandler(
                                                (request, response, error) -> {
                                                    if (String.valueOf(request.getHeader("Accept"))
                                                            .contains("application/json")) {
                                                        response.setStatus(403);
                                                        response.setContentType(
                                                                "application/json;charset=UTF-8");
                                                        response.getWriter()
                                                                .write(
                                                                        "{\"code\":\"FORBIDDEN\",\"message\":\"Không có quyền hoặc phiên làm việc đã hết hạn. Vui lòng tải lại trang.\"}");
                                                    } else {
                                                        response.sendError(403);
                                                    }
                                                }));
        return http.build();
    }
}
