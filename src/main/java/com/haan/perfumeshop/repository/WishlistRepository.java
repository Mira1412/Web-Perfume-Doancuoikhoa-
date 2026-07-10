package com.haan.perfumeshop.repository;

import com.haan.perfumeshop.model.Wishlist;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.model.Perfume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserOrderByNgayThemDesc(User user);
    Optional<Wishlist> findByUserAndPerfume(User user, Perfume perfume);
    boolean existsByUserAndPerfume(User user, Perfume perfume);
    void deleteByUserAndPerfume(User user, Perfume perfume);
}
