package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Cart;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CartPageController {

    // Sửa lỗi 3: Dùng Constructor Injection (Chuẩn bảo mật của Spring Boot) thay cho @Autowired
    private final CartService cartService;

    public CartPageController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public String showCartPage(HttpSession session, Model model) {
        // 1. Kiểm tra xem khách đã đăng nhập chưa
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login"; // Chưa đăng nhập thì đuổi về trang login
        }

        // 2. Lấy danh sách sản phẩm trong giỏ hàng của khách này
        List<Cart> cartItems = cartService.getCartByUserId(currentUser.getId_user());

        // 3. Tính toán tổng tiền
        double totalPrice = 0;
        if (cartItems != null) {
            for (Cart item : cartItems) {
                double price = 0;
                
                try {
                    // Sửa lỗi 1 & 2: Ép kiểu chuỗi chữ thành số toán học
                    if (item.getVariant() != null && item.getVariant().getGia_ban() != null) {
                        // Lọc bỏ mọi ký tự chữ (đ, ₫), dấu phẩy, khoảng trắng... chỉ giữ lại số nguyên
                        String priceStr = item.getVariant().getGia_ban().replaceAll("[^\\d]", "");
                        price = Double.parseDouble(priceStr);
                    } else if (item.getPerfume() != null && item.getPerfume().getVariants() != null && !item.getPerfume().getVariants().isEmpty()) {
                        // Nếu giỏ hàng lưu dữ liệu cũ (chưa có biến thể), tự động lấy giá của biến thể đầu tiên làm chuẩn
                        String priceStr = item.getPerfume().getVariants().get(0).getGia_ban().replaceAll("[^\\d]", "");
                        price = Double.parseDouble(priceStr);
                    }
                } catch (Exception e) {
                    price = 0; // Đề phòng lỗi (chưa nhập giá hoặc lỗi định dạng) thì cho giá = 0 để web không bị sập
                }
                
                // Thành tiền = Giá * Số lượng
                totalPrice += price * item.getSo_luong();
            }
        }

        // 4. Gửi dữ liệu ra file HTML
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);

        return "cart"; // Mở file cart.html trong thư mục templates
    }
}