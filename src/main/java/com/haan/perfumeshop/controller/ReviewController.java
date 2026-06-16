package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.model.Review;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.repository.OrderDetailRepository;
import com.haan.perfumeshop.repository.PerfumeRepository;
import com.haan.perfumeshop.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PerfumeRepository perfumeRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    // API Hứng dữ liệu khi khách gửi form đánh giá
    @PostMapping("/review/submit")
    public String submitReview(
            @RequestParam("perfumeId") Long perfumeId,
            @RequestParam("soSao") Integer soSao,
            @RequestParam("noiDung") String noiDung,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 1. Lấy thông tin User đang đăng nhập
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("reviewError", "Bạn cần đăng nhập để gửi đánh giá.");
            return "redirect:/login";
        }

        // 2. Tìm thông tin chai nước hoa
        Perfume perfume = perfumeRepository.findById(perfumeId).orElse(null);
        if (perfume == null) {
            redirectAttributes.addFlashAttribute("reviewError", "Không tìm thấy sản phẩm.");
            return "redirect:/";
        }

        // 3. Kiểm tra bảo mật kép: Khách đã mua hàng mới được đánh giá?
        boolean hasBought = orderDetailRepository.existsByUserAndPerfumeDelivered(currentUser.getId_user(), perfumeId);
        if (!hasBought) {
            redirectAttributes.addFlashAttribute("reviewError",
                    "Bạn chỉ được đánh giá những sản phẩm đã mua và nhận thành công.");
            return "redirect:/product/" + perfumeId;
        }

        // 4. Kiểm tra bảo mật kép: Khách đã từng đánh giá chai này chưa?
        boolean alreadyRated = reviewRepository.findReviewByUserAndPerfume(currentUser.getId_user(), perfumeId)
                .isPresent();
        if (alreadyRated) {
            redirectAttributes.addFlashAttribute("reviewError", "Bạn đã đánh giá sản phẩm này rồi.");
            return "redirect:/product/" + perfumeId;
        }

        // 5. Mọi thứ hợp lệ -> Tạo và lưu Review mới vào Database
        Review review = new Review();
        review.setUser(currentUser);
        review.setPerfume(perfume);
        review.setSo_sao(soSao);
        review.setNoi_dung(noiDung);
        reviewRepository.save(review);

        // 6. Trả thông báo thành công và load lại trang chi tiết
        redirectAttributes.addFlashAttribute("reviewSuccess", "Cảm ơn bạn đã đánh giá sản phẩm!");
        return "redirect:/product/" + perfumeId;
    }
}