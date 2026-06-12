package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Cart;
import com.haan.perfumeshop.model.Order;
import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.repository.OrderRepository;
import com.haan.perfumeshop.repository.PerfumeRepository;
import com.haan.perfumeshop.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private PerfumeRepository perfumeRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/")
    public String showHomePage(Model model) {
        List<Perfume> perfumes = perfumeRepository.findAll();
        model.addAttribute("perfumes", perfumes);
        return "index";
    }

    @GetMapping("/cart")
    public String showCartPage(HttpSession session, Model model) {
        // Lấy thông tin người dùng đang đăng nhập từ Session
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // Nếu chưa đăng nhập, bắt buộc quay về trang đăng nhập
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Lấy giỏ hàng theo ID của chính người dùng đang đăng nhập
        List<Cart> cartItems = cartService.getCartByUserId(loggedInUser.getId_user());

        double totalPrice = 0;
        for (Cart item : cartItems) {
            totalPrice += item.getPerfume().getGiaBanNumeric() * item.getSo_luong();
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);

        return "cart";
    }

    // ĐÃ BỔ SUNG: Hàm xử lý hiển thị trang Lịch sử đơn hàng
    @GetMapping("/orders")
    public String showOrderHistory(HttpSession session, Model model) {
        // Kiểm tra xem khách đã đăng nhập chưa
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login"; // Chưa đăng nhập thì đuổi về trang login
        }

        // Nhờ Repository tìm toàn bộ hóa đơn của vị khách này
        List<Order> myOrders = orderRepository.findByUser_Id_user(loggedInUser.getId_user());

        // Đẩy danh sách hóa đơn sang giao diện HTML
        model.addAttribute("orders", myOrders);

        return "orders"; // Trả về trang orders.html
    }

    // =======================================================
    // CHỨC NĂNG MỚI ĐÃ THÊM: Xem chi tiết sản phẩm nước hoa
    // =======================================================
    @GetMapping("/perfume/{id}")
    public String showPerfumeDetail(@PathVariable("id") Long id, Model model) {
        // 1. Tìm nước hoa trong cơ sở dữ liệu dựa trên id được truyền từ đường dẫn
        // (URL)
        Optional<Perfume> perfumeOpt = perfumeRepository.findById(id);

        // 2. Nếu không tìm thấy, bạn có thể chuyển hướng về trang chủ hoặc thông báo
        // lỗi
        if (perfumeOpt.isEmpty()) {
            return "redirect:/?error=not_found";
        }

        // 3. Lấy đối tượng Perfume thực tế từ Optional
        Perfume perfume = perfumeOpt.get();

        // 4. Gửi đối tượng nước hoa vừa tìm được sang giao diện HTML qua Model
        model.addAttribute("perfume", perfume);

        // 5. Trả về tên file HTML chi tiết (chi tiết sản phẩm hiển thị tại detail.html)
        return "detail";
    }
}