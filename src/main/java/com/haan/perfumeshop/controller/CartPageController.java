package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Cart;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CartPageController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cart")
    public String showCartPage(HttpSession session, Model model) {
        // 1. Kiểm tra xem khách đã đăng nhập chưa
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login"; // Chưa đăng nhập thì đuổi về trang login
        }

        // 2. Lấy danh sách sản phẩm trong giỏ hàng của khách này
        List<Cart> cartItems = cartService.getCartByUserId(currentUser.getId_user());

        // 3. Tính toán tổng tiền (Cập nhật logic lấy giá của Biến thể 50ml/100ml)
        double totalPrice = 0;
        if (cartItems != null) {
            for (Cart item : cartItems) {
                double price = 0;
                // Nếu khách mua dung tích (biến thể), lấy giá của biến thể
                if (item.getVariant() != null) {
                    price = item.getVariant().getGia_ban(); 
                } 
                // Nếu khách mua chai gốc (không chọn dung tích), lấy giá gốc
                else {
                    price = item.getPerfume().getGiaBanNumeric();
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