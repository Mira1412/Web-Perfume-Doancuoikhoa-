package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Order;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.service.OrderService;
import com.haan.perfumeshop.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<String> testEmail(HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập trước khi kiểm tra!");
        }
        if (currentUser.getEmail() == null || currentUser.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tài khoản đăng nhập của bạn hiện không có email hợp lệ!");
        }
        try {
            emailService.sendTestEmailSync(currentUser.getEmail());
            return ResponseEntity.ok("✅ Gửi email kiểm tra thành công tới địa chỉ: " + currentUser.getEmail());
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Gửi email thất bại!\n\nLỗi: " + e.getMessage() + "\n\nChi tiết Stacktrace:\n" + sw.toString());
        }
    }
}