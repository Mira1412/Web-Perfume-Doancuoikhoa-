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

    @PostMapping("/review/submit")
    public String submitReview(
            @RequestParam("perfumeId") Long perfumeId,
            @RequestParam("soSao") Integer soSao,
            @RequestParam("noiDung") String noiDung,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 1. Kiểm tra đăng nhập
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // 2. Kiểm tra số sao hợp lệ
        if (soSao == null || soSao < 1 || soSao > 5) {
            redirectAttributes.addFlashAttribute("reviewError", "Vui lòng chọn số sao từ 1 đến 5.");
            return "redirect:/product/" + perfumeId;
        }

        // 3. Kiểm tra đã mua hàng chưa
        boolean hasBought = orderDetailRepository.existsByUserAndPerfumeDelivered(
                currentUser.getId_user(), perfumeId);
        if (!hasBought) {
            redirectAttributes.addFlashAttribute("reviewError", "Bạn cần mua và nhận sản phẩm này trước khi đánh giá.");
            return "redirect:/product/" + perfumeId;
        }

        // 4. Kiểm tra đã đánh giá chưa (ĐÃ CẬP NHẬT TÊN HÀM MỚI TẠI ĐÂY)
        boolean alreadyRated = reviewRepository
                .findReviewByUserAndPerfume(currentUser.getId_user(), perfumeId)
                .isPresent();
        if (alreadyRated) {
            redirectAttributes.addFlashAttribute("reviewError", "Bạn đã đánh giá sản phẩm này rồi.");
            return "redirect:/product/" + perfumeId;
        }

        // 5. Tìm sản phẩm
        Perfume perfume = perfumeRepository.findById(perfumeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        // 6. Lưu đánh giá
        Review review = new Review();
        review.setUser(currentUser);
        review.setPerfume(perfume);
        review.setSo_sao(soSao);
        review.setNoi_dung(noiDung != null ? noiDung.trim() : "");
        reviewRepository.save(review);

        redirectAttributes.addFlashAttribute("reviewSuccess", "Cảm ơn bạn đã đánh giá sản phẩm! ⭐");
        return "redirect:/product/" + perfumeId;
    }
}