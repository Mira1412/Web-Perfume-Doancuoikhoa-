package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Order;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // API Chốt đơn hàng (Kiểm tra tồn kho + Trừ kho + Tạo hóa đơn): POST http://localhost:8082/api/orders/checkout
    @PostMapping("/checkout")
    public ResponseEntity<?> checkoutOrder(@RequestBody User user) {
        try {
            Order placedOrder = orderService.checkoutOrder(user);
            return ResponseEntity.ok(placedOrder);
        } catch (Exception e) {
            // Trả về thông báo lỗi trực tiếp nếu kho không đủ hàng hoặc giỏ trống
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}