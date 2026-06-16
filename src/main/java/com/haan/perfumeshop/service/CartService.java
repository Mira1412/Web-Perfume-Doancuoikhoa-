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

    // Lấy toàn bộ sản phẩm trong giỏ hàng của 1 khách hàng
    public List<Cart> getCartByUserId(Long idUser) {
        return cartRepository.findByUser_Id_user(idUser);
    }

    // Logic thêm đồ vào giỏ hàng (ĐÃ CẬP NHẬT LOGIC DUNG TÍCH)
    public Cart addToCart(Cart cart) {
        // 1. Kiểm tra an toàn dữ liệu đầu vào
        if (cart.getUser() == null || cart.getUser().getId_user() == null ||
                cart.getPerfume() == null || cart.getPerfume().getId_nuoc_hoa() == null) {
            throw new IllegalArgumentException("Thông tin User hoặc Nước hoa không được để trống!");
        }

        // 2. Lấy giỏ hàng hiện tại của khách
        List<Cart> currentCart = cartRepository.findByUser_Id_user(cart.getUser().getId_user());

        // 3. Nếu giỏ hàng hiện tại không trống, duyệt qua để check trùng
        if (currentCart != null) {
            for (Cart item : currentCart) {
                // Kiểm tra có trùng ID nước hoa (chai gốc) hay không
                if (item.getPerfume() != null &&
                        item.getPerfume().getId_nuoc_hoa().equals(cart.getPerfume().getId_nuoc_hoa())) {

                    // ========================================================
                    // KIỂM TRA TRÙNG DUNG TÍCH (BIẾN THỂ)
                    // ========================================================
                    boolean isSameVariant = false;

                    // Trường hợp 1: Cả 2 đều không chọn dung tích (mua bản gốc)
                    if (item.getVariant() == null && cart.getVariant() == null) {
                        isSameVariant = true;
                    }
                    // Trường hợp 2: Cả 2 đều có chọn dung tích VÀ ID dung tích giống hệt nhau
                    else if (item.getVariant() != null && cart.getVariant() != null &&
                            item.getVariant().getId_bien_the().equals(cart.getVariant().getId_bien_the())) {
                        isSameVariant = true;
                    }

                    // Nếu trùng hoàn toàn (cùng chai, cùng dung tích) -> Cộng dồn số lượng
                    if (isSameVariant) {
                        item.setSo_luong(item.getSo_luong() + cart.getSo_luong());
                        return cartRepository.save(item);
                    }
                }
            }
        }

        // 4. Nếu chưa có món này (hoặc khác dung tích), lưu thành 1 dòng mới hoàn toàn
        return cartRepository.save(cart);
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    public Cart updateQuantity(Long idGioHang, int newQuantity) {
        Cart cart = cartRepository.findById(idGioHang).orElse(null);
        if (cart != null && newQuantity > 0) {
            cart.setSo_luong(newQuantity);
            return cartRepository.save(cart);
        }
        return cart;
    }

    // Xóa một món đồ khỏi giỏ hàng
    public void removeFromCart(Long idGioHang) {
        cartRepository.deleteById(idGioHang);
    }

    // Xóa sạch giỏ hàng của khách sau khi chốt đơn
    public void clearCart(Long idUser) {
        List<Cart> userCart = cartRepository.findByUser_Id_user(idUser);
        cartRepository.deleteAll(userCart);
    }
}