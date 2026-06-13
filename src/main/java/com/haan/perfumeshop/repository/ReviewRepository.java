package com.haan.perfumeshop.repository;

import com.haan.perfumeshop.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 1. Kiểm tra khách hàng đã đánh giá chai nước hoa này chưa
    @Query("SELECT r FROM Review r WHERE r.user.id_user = :userId AND r.perfume.id_nuoc_hoa = :perfumeId")
    Optional<Review> findReviewByUserAndPerfume(@Param("userId") Long userId, @Param("perfumeId") Long perfumeId);

    // 2. Lấy danh sách đánh giá của 1 chai nước hoa (sắp xếp mới nhất)
    @Query("SELECT r FROM Review r WHERE r.perfume.id_nuoc_hoa = :perfumeId ORDER BY r.ngay_tao DESC")
    List<Review> findByPerfumeIdOrderByNgayTaoDesc(@Param("perfumeId") Long perfumeId);

    // 3. Tính điểm đánh giá trung bình
    @Query("SELECT AVG(r.so_sao) FROM Review r WHERE r.perfume.id_nuoc_hoa = :perfumeId")
    Double findAverageRatingByPerfumeId(@Param("perfumeId") Long perfumeId);

    // 4. Đếm số lượng đánh giá theo từng mức sao (1 sao -> 5 sao)
    @Query("SELECT r.so_sao, COUNT(r) FROM Review r WHERE r.perfume.id_nuoc_hoa = :perfumeId GROUP BY r.so_sao")
    List<Object[]> countBySoSaoForPerfume(@Param("perfumeId") Long perfumeId);
}