package com.haan.perfumeshop.service;

import com.haan.perfumeshop.model.Wishlist;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.repository.WishlistRepository;
import com.haan.perfumeshop.repository.PerfumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final PerfumeRepository perfumeRepository;

    public WishlistService(WishlistRepository wishlistRepository, PerfumeRepository perfumeRepository) {
        this.wishlistRepository = wishlistRepository;
        this.perfumeRepository = perfumeRepository;
    }

    @Transactional
    public boolean toggleWishlist(User user, Long perfumeId) {
        Perfume perfume = perfumeRepository.findById(perfumeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm nước hoa!"));

        Optional<Wishlist> existing = wishlistRepository.findByUserAndPerfume(user, perfume);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            return false; // Đã xóa khỏi danh sách yêu thích
        } else {
            Wishlist wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlist.setPerfume(perfume);
            wishlistRepository.save(wishlist);
            return true; // Đã thêm vào danh sách yêu thích
        }
    }

    public List<Wishlist> getWishlistByUser(User user) {
        return wishlistRepository.findByUserOrderByNgayThemDesc(user);
    }

    public boolean isWishlisted(User user, Long perfumeId) {
        if (user == null) return false;
        Perfume perfume = perfumeRepository.findById(perfumeId).orElse(null);
        if (perfume == null) return false;
        return wishlistRepository.existsByUserAndPerfume(user, perfume);
    }
}
