package com.haan.perfumeshop.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // Kiểm tra xem trong Session có user và role là ADMIN không
        Object user = request.getSession().getAttribute("loggedInUser");

        // Giả sử bạn lưu Role trong thuộc tính của User hoặc Session
        if (user != null && "ADMIN".equals(request.getSession().getAttribute("userRole"))) {
            return true; // Cho phép đi tiếp
        }

        response.sendRedirect("/login"); // Nếu không phải Admin, tống cổ về trang đăng nhập
        return false;
    }
}