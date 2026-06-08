package com.haan.perfumeshop.service;

import com.haan.perfumeshop.model.Cart;
import com.haan.perfumeshop.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    // Lấy toàn bộ sản phẩm trong giỏ hàng của 1 khách hàng để hiển thị ra giao diện UI
    public List<Cart> getCartByUserId(Long idUser) {
        return cartRepository.findByUser_Id_user(idUser);
    }

    // Logic thêm đồ vào giỏ hàng (Tự động cộng dồn nếu trùng sản phẩm)
   public Cart addToCart(Cart cart) {
    // 1. Kiểm tra an toàn dữ liệu đầu vào để tránh lỗi NullPointerException
    if (cart.getUser() == null || cart.getUser().getId_user() == null ||
        cart.getPerfume() == null || cart.getPerfume().getId_nuoc_hoa() == null) {
        throw new IllegalArgumentException("Thông tin User hoặc Nước hoa không được để trống!");
    }

    // 2. Lấy giỏ hàng hiện tại của khách
    List<Cart> currentCart = cartRepository.findByUser_Id_user(cart.getUser().getId_user());
    
    // 3. Nếu giỏ hàng hiện tại không trống, duyệt qua để check trùng
    if (currentCart != null) {
        for (Cart item : currentCart) {
            if (item.getPerfume() != null && 
                item.getPerfume().getId_nuoc_hoa().equals(cart.getPerfume().getId_nuoc_hoa())) {
                
                item.setSo_luong(item.getSo_luong() + cart.getSo_luong());
                return cartRepository.save(item);
            }
        }
    }
    
    // 4. Nếu chưa có món này trong giỏ, lưu mới hoàn toàn
    return cartRepository.save(cart);
}

    // Xóa một món đồ khỏi giỏ hàng
    public void removeFromCart(Long idGioHang) {
        cartRepository.deleteById(idGioHang);
    }

    // Xóa sạch giỏ hàng của khách sau khi họ đã bấm chốt đơn thanh toán thành công
    public void clearCart(Long idUser) {
        List<Cart> userCart = cartRepository.findByUser_Id_user(idUser);
        cartRepository.deleteAll(userCart);
    }
}