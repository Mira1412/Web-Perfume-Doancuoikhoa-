package com.haan.perfumeshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Tạo Bean mã hóa mật khẩu để gọi dùng ở khắp nơi
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Cấu hình cho phép tất cả các URL chạy qua tự do để giữ nguyên logic Session
    // cũ của bạn
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF để tránh lỗi khi gửi các form POST
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Mở khóa hoàn toàn tất cả các trang
                );
        return http.build();
    }
}