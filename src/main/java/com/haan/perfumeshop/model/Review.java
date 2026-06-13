package com.haan.perfumeshop.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews",
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_user", "id_nuoc_hoa"})) // Mỗi user chỉ review 1 lần / 1 sản phẩm
@Data
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người đánh giá
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    // Sản phẩm được đánh giá
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nuoc_hoa", nullable = false)
    private Perfume perfume;

    // Số sao (1–5)
    @Column(nullable = false)
    private Integer so_sao;

    // Nội dung bình luận
    @Column(columnDefinition = "TEXT")
    private String noi_dung;

    // Thời gian tạo — tự động gán khi lưu
    private LocalDateTime ngay_tao;

    @PrePersist
    protected void onCreate() {
        this.ngay_tao = LocalDateTime.now();
    }
}
