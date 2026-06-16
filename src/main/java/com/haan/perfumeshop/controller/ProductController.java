package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.model.Review;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.repository.OrderDetailRepository;
import com.haan.perfumeshop.repository.PerfumeRepository;
import com.haan.perfumeshop.repository.PerfumeSpecification;
import com.haan.perfumeshop.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    private static final int PAGE_SIZE = 12;

    @Autowired
    private PerfumeRepository perfumeRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @GetMapping({ "", "/", "/index" })
    public String showHomePage(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "thuong_hieu", required = false) String thuongHieu,
            @RequestParam(value = "nhom_huong", required = false) String nhomHuong,
            @RequestParam(value = "gia_range", required = false) String giaRange,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        List<Perfume> filtered;
        if (keyword != null && !keyword.trim().isEmpty()) {
            filtered = perfumeRepository.searchPerfumes(keyword.trim());
            model.addAttribute("keyword", keyword);
        } else {
            filtered = perfumeRepository.findAll(PerfumeSpecification.filter(thuongHieu, nhomHuong));
        }

        if (giaRange != null && !giaRange.isEmpty()) {
            filtered = filtered.stream().filter(p -> {
                double gia = p.getGiaBanNumeric();
                switch (giaRange) {
                    case "duoi1tr":
                        return gia > 0 && gia < 1_000_000;
                    case "1tr_3tr":
                        return gia >= 1_000_000 && gia <= 3_000_000;
                    case "3tr_5tr":
                        return gia > 3_000_000 && gia <= 5_000_000;
                    case "tren5tr":
                        return gia > 5_000_000;
                    default:
                        return true;
                }
            }).collect(Collectors.toList());
        }

        int totalItems = filtered.size();
        int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);
        if (page < 0)
            page = 0;
        if (totalPages > 0 && page >= totalPages)
            page = totalPages - 1;

        int fromIndex = page * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, totalItems);

        model.addAttribute("perfumes", filtered.subList(fromIndex, toIndex));
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("thuongHieus", perfumeRepository.findDistinctThuongHieu());
        model.addAttribute("nhomHuongs", perfumeRepository.findDistinctNhomHuong());
        model.addAttribute("selectedThuongHieu", thuongHieu);
        model.addAttribute("selectedNhomHuong", nhomHuong);
        model.addAttribute("selectedGiaRange", giaRange);
        return "index";
    }

    @GetMapping("/product/{id}")
    public String showProductDetail(@PathVariable("id") Long id,
            HttpSession session,
            Model model) {

        Perfume perfume = perfumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nước hoa này!"));
        model.addAttribute("perfume", perfume);

        // ---- Load reviews (Đã sửa tên hàm) ----
        List<Review> reviews = reviewRepository.findByPerfumeIdOrderByNgayTaoDesc(id);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewCount", reviews.size());

        // ---- Điểm trung bình (Đã sửa tên hàm) ----
        Double avgRating = reviewRepository.findAverageRatingByPerfumeId(id);
        model.addAttribute("avgRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);

        // ---- Phân bố theo mức sao (Đã sửa tên hàm) ----
        Map<Integer, Long> starDist = new HashMap<>();
        for (int i = 1; i <= 5; i++)
            starDist.put(i, 0L);
        reviewRepository.countBySoSaoForPerfume(id)
                .forEach(row -> starDist.put((Integer) row[0], (Long) row[1]));
        model.addAttribute("starDist", starDist);

        // ---- Kiểm tra quyền đánh giá ----
        User currentUser = (User) session.getAttribute("loggedInUser");
        boolean canReview = false;
        boolean alreadyRated = false;

        if (currentUser != null) {
            boolean hasBought = orderDetailRepository.existsByUserAndPerfumeDelivered(
                    currentUser.getId_user(), id);

            // Đã sửa tên hàm kiểm tra review
            alreadyRated = reviewRepository
                    .findReviewByUserAndPerfume(currentUser.getId_user(), id)
                    .isPresent();

            canReview = hasBought && !alreadyRated;
        }

        model.addAttribute("canReview", canReview);
        model.addAttribute("alreadyRated", alreadyRated);
        model.addAttribute("isLoggedIn", currentUser != null);

        return "detail";
    }
}