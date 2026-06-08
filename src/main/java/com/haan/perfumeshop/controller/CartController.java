package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Cart;
import com.haan.perfumeshop.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/user/{idUser}")
    public ResponseEntity<List<Cart>> getCartByUserId(@PathVariable Long idUser) {
        return ResponseEntity.ok(cartService.getCartByUserId(idUser));
    }

    // ĐÃ SỬA: Thêm Try-Catch để bắt hệ thống in thẳng lỗi ra Postman
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Cart cart) {
        try {
            Cart savedCart = cartService.addToCart(cart);
            return ResponseEntity.ok(savedCart);
        } catch (Exception e) {
            // In chi tiết lỗi ra màn hình Terminal của VS Code
            e.printStackTrace(); 
            
            // Trả dòng text mô tả lỗi thẳng về Postman
            String errorMessage = e.getMessage();
            if (e.getCause() != null && e.getCause().getCause() != null) {
                errorMessage = e.getCause().getCause().getMessage(); // Lấy lỗi sâu nhất của Database nếu có
            }
            return ResponseEntity.status(500).body("Chi tiết lỗi: " + errorMessage);
        }
    }

    @DeleteMapping("/remove/{idGioHang}")
    public ResponseEntity<String> removeFromCart(@PathVariable Long idGioHang) {
        cartService.removeFromCart(idGioHang);
        return ResponseEntity.ok("Đã xóa sản phẩm khỏi giỏ hàng thành công!");
    }
}