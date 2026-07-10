package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.model.Wishlist;
import com.haan.perfumeshop.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public String showWishlistPage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        List<Wishlist> wishlists = wishlistService.getWishlistByUser(loggedInUser);
        model.addAttribute("wishlistItems", wishlists);
        model.addAttribute("user", loggedInUser);
        return "wishlist";
    }

    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleWishlist(@RequestParam("perfumeId") Long perfumeId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập để thực hiện chức năng này!"));
        }
        try {
            boolean isAdded = wishlistService.toggleWishlist(loggedInUser, perfumeId);
            return ResponseEntity.ok(Map.of("added", isAdded, "message", isAdded ? "Đã thêm vào danh sách yêu thích!" : "Đã xóa khỏi danh sách yêu thích!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
