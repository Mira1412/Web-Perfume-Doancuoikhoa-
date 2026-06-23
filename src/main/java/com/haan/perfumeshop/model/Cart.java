package com.haan.perfumeshop.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "carts")
@Data
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gio_hang")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_nuoc_hoa", nullable = false)
    private Perfume perfume;

    @Column(nullable = false)
    private Integer so_luong;

    // ==============================================================
    // Liên kết với bảng biến thể (50ml, 100ml)
    // ==============================================================
    @ManyToOne
    @JoinColumn(name = "id_bien_the", nullable = true) 
    private PerfumeVariant variant;

    // ==============================================================
    // TỰ VIẾT HÀM GET/SET ĐỂ KHẮC PHỤC LỖI "UNDEFINED" CỦA VS CODE
    // ==============================================================
    public PerfumeVariant getVariant() {
        return this.variant;
    }

    public void setVariant(PerfumeVariant variant) {
        this.variant = variant;
    }

    // ==============================================================
    // TIỆN ÍCH: Lấy giá bán dưới dạng số thực tế để tính toán/hiển thị
    // ==============================================================
    public double getPriceNumeric() {
        if (variant != null && variant.getGia_ban() != null) {
            try {
                String priceStr = variant.getGia_ban().replaceAll("[^\\d]", "");
                return Double.parseDouble(priceStr);
            } catch (Exception e) {
                return 0.0;
            }
        }
        if (perfume != null && perfume.getVariants() != null && !perfume.getVariants().isEmpty()) {
            try {
                String priceStr = perfume.getVariants().get(0).getGia_ban().replaceAll("[^\\d]", "");
                return Double.parseDouble(priceStr);
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}