package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Cart;
import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.model.PerfumeVariant;
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

    // ==============================================================
    // BỔ SUNG: DTO để hứng chính xác dữ liệu từ hàm JS (detail.html / index.html) gửi lên
    // ==============================================================
    public static class CartRequest {
        public User user;
        public Perfume perfume;
        public Long id_bien_the; // Bắt lấy ID dung tích khách chọn (từ detail.html)
        public Long variantId;   // Bắt lấy ID dung tích khách chọn (từ index.html)
        public Integer so_luong;
    }

    @GetMapping("/user/{idUser}")
    public ResponseEntity<List<Cart>> getCartByUserId(@PathVariable Long idUser) {
        return ResponseEntity.ok(cartService.getCartByUserId(idUser));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartRequest request) {
        try {
            // 1. Khởi tạo một giỏ hàng mới và lắp ráp dữ liệu từ request vào
            Cart cart = new Cart();
            cart.setUser(request.user);
            cart.setPerfume(request.perfume);
            cart.setSo_luong(request.so_luong != null ? request.so_luong : 1);

            // 2. Nếu khách có chọn dung tích, thì nhét nó vào Giỏ
            Long selectedVariantId = request.id_bien_the != null ? request.id_bien_the : request.variantId;
            if (selectedVariantId != null) {
                PerfumeVariant variant = new PerfumeVariant();
                variant.setId_bien_the(selectedVariantId);
                cart.setVariant(variant);
            }

            // 3. Giao cho Service xử lý như cũ
            Cart savedCart = cartService.addToCart(cart);
            return ResponseEntity.ok(savedCart);

        } catch (Exception e) {
            // In chi tiết lỗi ra màn hình Terminal của VS Code
            e.printStackTrace();

            // Trả dòng text mô tả lỗi thẳng về Postman hoặc Giao diện web
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
    }    @PutMapping("/update/{idGioHang}")
    public ResponseEntity<String> updateQuantity(@PathVariable Long idGioHang, @RequestParam("quantity") int quantity) {
        try {
            Cart updated = cartService.updateQuantity(idGioHang, quantity);
            if (updated != null) {
                return ResponseEntity.ok("Cập nhật số lượng thành công!");
            }
            return ResponseEntity.status(400).body("Không thể cập nhật số lượng!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/count/{idUser}")
    public ResponseEntity<Integer> getCartCount(@PathVariable Long idUser) {
        try {
            List<Cart> items = cartService.getCartByUserId(idUser);
            int total = items.stream().mapToInt(Cart::getSo_luong).sum();
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            return ResponseEntity.ok(0);
        }
    }
}
