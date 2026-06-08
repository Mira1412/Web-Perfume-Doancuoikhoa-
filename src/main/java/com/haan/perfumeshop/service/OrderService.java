package com.haan.perfumeshop.service;

import com.haan.perfumeshop.model.*;
import com.haan.perfumeshop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private PerfumeRepository perfumeRepository;

    @Autowired
    private CartService cartService;

    // Logic Chốt Đơn Hàng từ Giỏ Hàng (Cài đặt Quy tắc 2 - Kiểm tra và trừ tồn kho)
    @Transactional(rollbackFor = Exception.class) // Đảm bảo nếu lỗi ở bất kỳ bước nào, toàn bộ giao dịch sẽ hủy bỏ (Rollback)
    public Order checkoutOrder(User user) throws Exception {
        // 1. Lấy toàn bộ món đồ trong giỏ của khách ra
        List<Cart> cartItems = cartService.getCartByUserId(user.getId_user());
        if (cartItems.isEmpty()) {
            throw new Exception("Giỏ hàng của bạn đang trống, không thể đặt hàng!");
        }

        // 2. Kiểm tra hàng tồn kho trước khi tạo hóa đơn (Quy tắc 2)
        for (Cart item : cartItems) {
            Perfume perfume = item.getPerfume();
            if (perfume.getTon_kho() < item.getSo_luong()) {
                throw new Exception("Sản phẩm '" + perfume.getTen_sp() + "' không đủ số lượng trong kho! (Hiện còn: " + perfume.getTon_kho() + ")");
            }
        }

        // 3. Nếu mọi thứ hợp lệ, tiến hành tạo đơn hàng tổng (Order)
        Order order = new Order();
        order.setUser(user);
        order.setTrang_thai("Pending");
        Order savedOrder = orderRepository.save(order);

        double tongTien = 0.0;
        // 4. Lưu chi tiết đơn hàng và thực hiện trừ số lượng tồn kho vật lý
        for (Cart item : cartItems) {
            Perfume perfume = item.getPerfume();
            
            // Tạo chi tiết đơn
            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);
            detail.setPerfume(perfume);
            detail.setSo_luong_mua(item.getSo_luong());
            detail.setGia_luc_mua(perfume.getGia_ban()); // Chốt giữ nguyên giá bán tại thời điểm mua
            orderDetailRepository.save(detail);

            tongTien += (item.getSo_luong() * perfume.getGia_ban());

            // Cập nhật lại số lượng tồn kho mới sau khi trừ
            perfume.setTon_kho(perfume.getTon_kho() - item.getSo_luong());
            perfumeRepository.save(perfume);
        }

        // Cập nhật tổng tiền cho hóa đơn
        savedOrder.setTong_tien(tongTien);
        orderRepository.save(savedOrder);

        // 5. Đặt hàng hoàn tất, tiến hành xóa sạch giỏ hàng tạm thời
        cartService.clearCart(user.getId_user());

        return savedOrder;
    }
}