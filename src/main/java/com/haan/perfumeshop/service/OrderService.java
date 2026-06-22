package com.haan.perfumeshop.service;

import com.haan.perfumeshop.model.*;
import com.haan.perfumeshop.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OrderService {

    // 1. SỬA CẢNH BÁO VÀNG: Gom tất cả @Autowired thành Constructor Injection
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PerfumeRepository perfumeRepository;
    private final CartService cartService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository,
            PerfumeRepository perfumeRepository,
            CartService cartService,
            EmailService emailService,
            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.perfumeRepository = perfumeRepository;
        this.cartService = cartService;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    // Logic Chốt Đơn Hàng từ Giỏ Hàng
    @Transactional(rollbackFor = Exception.class)
    public Order checkoutOrder(User user) throws Exception {
        // 0. Lấy thông tin User đầy đủ từ Database
        User fullUser = userRepository.findById(user.getId_user()).orElse(user);

        // 1. Lấy toàn bộ món đồ trong giỏ của khách ra
        List<Cart> cartItems = cartService.getCartByUserId(fullUser.getId_user());
        if (cartItems.isEmpty()) {
            throw new Exception("Giỏ hàng của bạn đang trống, không thể đặt hàng!");
        }

        // 2. Kiểm tra hàng tồn kho trước khi tạo hóa đơn
        // (Lưu ý: Tạm thời vẫn trừ tồn kho ở chai gốc. Nếu sau này bạn muốn trừ tồn kho
        // chi tiết theo từng biến thể dung tích, bạn sẽ cập nhật thêm ở đây).
        for (Cart item : cartItems) {
            Perfume perfume = item.getPerfume();
            if (perfume.getTon_kho() < item.getSo_luong()) {
                throw new Exception("Sản phẩm '" + perfume.getTen_sp() + "' không đủ số lượng trong kho! (Hiện còn: "
                        + perfume.getTon_kho() + ")");
            }
        }

        // 3. Nếu mọi thứ hợp lệ, tiến hành tạo đơn hàng tổng (Order)
        Order order = new Order();
        order.setUser(fullUser);
        order.setTrang_thai("Pending");
        Order savedOrder = orderRepository.save(order);

        double tongTien = 0.0;

        // 4. Lưu chi tiết đơn hàng và thực hiện trừ số lượng tồn kho vật lý
        for (Cart item : cartItems) {
            Perfume perfume = item.getPerfume();
            double giaBanSo = 0;

            // =========================================================
            // 2. SỬA LỖI ĐỎ: Xử lý chuỗi giá bán thành số để tính toán
            // =========================================================
            try {
                if (item.getVariant() != null && item.getVariant().getGia_ban() != null) {
                    // Lấy giá từ biến thể và ép kiểu thành số
                    String priceStr = item.getVariant().getGia_ban().replaceAll("[^\\d]", "");
                    giaBanSo = Double.parseDouble(priceStr);
                } else if (perfume.getVariants() != null && !perfume.getVariants().isEmpty()) {
                    // Nếu không có biến thể cụ thể, lấy giá của biến thể đầu tiên làm mặc định
                    String priceStr = perfume.getVariants().get(0).getGia_ban().replaceAll("[^\\d]", "");
                    giaBanSo = Double.parseDouble(priceStr);
                }
            } catch (Exception e) {
                giaBanSo = 0; // Chống sập hệ thống nếu lỗi parse số
            }
            // =========================================================

            // Tạo chi tiết đơn
            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);
            detail.setPerfume(perfume);
            detail.setSo_luong_mua(item.getSo_luong());
            detail.setGia_luc_mua(giaBanSo);
            orderDetailRepository.save(detail);

            tongTien += (item.getSo_luong() * giaBanSo);

            // Cập nhật lại số lượng tồn kho mới sau khi trừ
            perfume.setTon_kho(perfume.getTon_kho() - item.getSo_luong());
            perfumeRepository.save(perfume);
        }

        // Cập nhật tổng tiền cho hóa đơn
        savedOrder.setTong_tien(tongTien);
        orderRepository.save(savedOrder);

        // 5. Đặt hàng hoàn tất, tiến hành xóa sạch giỏ hàng tạm thời
        cartService.clearCart(fullUser.getId_user());

        // 6. Gửi email xác nhận đơn hàng cho khách
        try {
            emailService.sendOrderConfirmationEmail(savedOrder);
        } catch (Exception e) {
            // Nếu gửi mail thất bại thì vẫn cho đặt hàng thành công, chỉ in log lỗi
            System.out.println("⚠️ Gửi email xác nhận thất bại: " + e.getMessage());
        }

        return savedOrder;
    }
}