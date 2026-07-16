package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Order;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.service.OrderService;
import com.haan.perfumeshop.service.EmailService;
import com.haan.perfumeshop.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    // API Chốt đơn hàng (Kiểm tra tồn kho + Trừ kho + Tạo hóa đơn): POST http://localhost:8081/api/orders/checkout
    @PostMapping("/checkout")
    public ResponseEntity<?> checkoutOrder(@RequestBody User user) {
        try {
            Order placedOrder = orderService.checkoutOrder(user, "COD", null);
            return ResponseEntity.ok(placedOrder);
        } catch (Exception e) {
            // Trả về thông báo lỗi trực tiếp nếu kho không đủ hàng hoặc giỏ trống
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API Hủy đơn hàng dành cho khách: POST http://localhost:8081/api/orders/{id}/cancel
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập!");
        }
        try {
            Order cancelledOrder = orderService.cancelOrder(id, currentUser);
            return ResponseEntity.ok(cancelledOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET Endpoint test cấu hình SMTP đồng bộ
    @GetMapping("/test-email")
    public ResponseEntity<String> testEmail(
            @RequestParam(value = "to", required = false) String to,
            HttpSession session) {
        String targetEmail = to;
        if (targetEmail == null || targetEmail.trim().isEmpty()) {
            User currentUser = (User) session.getAttribute("loggedInUser");
            if (currentUser != null) {
                targetEmail = currentUser.getEmail();
            }
        }
        if (targetEmail == null || targetEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng truyền email cần nhận test qua tham số ?to=email_cua_ban (Ví dụ: /api/orders/test-email?to=lucy139200556@gmail.com)");
        }
        try {
            emailService.sendTestEmailSync(targetEmail.trim());
            return ResponseEntity.ok("✅ Gửi email kiểm tra thành công tới địa chỉ: " + targetEmail);
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Gửi email thất bại!\n\n"
                            + "--- THÔNG TIN CẤU HÌNH SMTP ĐANG CHẠY TRONG APP ---\n"
                            + "Username: " + mailUsername + "\n"
                            + "Password: " + mailPassword + "\n"
                            + "------------------------------------------------\n\n"
                            + "Lỗi: " + e.getMessage() + "\n\n"
                            + "Chi tiết Stacktrace:\n" + sw.toString());
        }
    }

    // GET Endpoint hỗ trợ khôi phục khẩn cấp mật khẩu về '123456' trực tiếp
    @GetMapping("/reset-password-debug")
    public ResponseEntity<String> resetPasswordDebug(@RequestParam("email") String email) {
        if (email != null) {
            email = email.trim();
        }
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("❌ Không tìm thấy người dùng với email: " + email);
        }
        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);
        return ResponseEntity.ok("✅ Đã khôi phục (reset) mật khẩu của tài khoản " + email + " về mặc định: '123456' thành công! Bạn có thể dùng mật khẩu này để đăng nhập ngay lập tức.");
    }
}