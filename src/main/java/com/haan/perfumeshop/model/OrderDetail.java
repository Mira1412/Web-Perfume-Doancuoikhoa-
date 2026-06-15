package com.haan.perfumeshop.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "order_details")
@Data
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chi_tiet")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_don_hang", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "id_nuoc_hoa", nullable = false)
    private Perfume perfume;

    // =========================================================
    // BỔ SUNG: Lưu lại Biến thể (Dung tích) khách mua
    // =========================================================
    @ManyToOne
    @JoinColumn(name = "id_bien_the", nullable = true)
    private PerfumeVariant variant;

    @Column(nullable = false)
    private Integer so_luong_mua;

    @Column(nullable = false)
    private Double gia_luc_mua;

    // Tự viết Get/Set cho variant để tránh lỗi Undefined của Lombok (nếu có)
    public PerfumeVariant getVariant() {
        return this.variant;
    }

    public void setVariant(PerfumeVariant variant) {
        this.variant = variant;
    }
}