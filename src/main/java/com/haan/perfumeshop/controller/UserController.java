package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    // ==========================================
    // PHẦN 1: LOGIC ĐĂNG NHẬP & ĐĂNG XUẤT
    // ==========================================

    // Hiển thị trang Đăng nhập
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // Xử lý khi người dùng bấm nút Đăng nhập
    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        try {
            User loggedInUser = userService.loginUser(email, password);
            // Lưu thông tin người dùng vào Session để mang đi khắp các trang
            session.setAttribute("loggedInUser", loggedInUser);
            return "redirect:/"; // Đăng nhập thành công thì quay về trang chủ
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "login"; // Đăng nhập lỗi thì đứng lại trang login và báo lỗi
        }
    }

    // Xử lý Đăng xuất
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("loggedInUser"); // Xóa trí nhớ của hệ thống về user này
        return "redirect:/";
    }

    // ==========================================
    // PHẦN 2: LOGIC ĐĂNG KÝ TÀI KHOẢN MỚI
    // ==========================================

    // Hiển thị trang Đăng ký
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    // Xử lý khi người dùng bấm nút Đăng ký
    @PostMapping("/register")
    public String processRegister(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam(required = false, defaultValue = "") String phone,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {
        try {
            userService.registerUser(fullName, email, phone, password, confirmPassword);

            // Đăng ký thành công → chuyển sang trang login kèm thông báo
            model.addAttribute("success", "🎉 Chúc mừng! Tài khoản đã được tạo thành công. Mời bạn đăng nhập.");
            return "login";

        } catch (Exception e) {
            // Thất bại → đứng lại trang register, giữ lại giá trị đã nhập để tiện sửa
            model.addAttribute("error", e.getMessage());
            model.addAttribute("fullNameValue", fullName);
            model.addAttribute("emailValue", email);
            model.addAttribute("phoneValue", phone);
            return "register";
        }
    }
}