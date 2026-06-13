package com.haan.perfumeshop.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.util.List;

@Entity
@Table(name = "perfumes")
@Data
public class Perfume {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_nuoc_hoa;

    private String ten_sp;
    private String thuong_hieu;
    private String nhom_huong;
    private String dung_tich;
    private String gia_ban; // Đã đổi sang String ở bước trước
    private Integer ton_kho;
    private String hinh_anh;

    // ==============================================================
    // BỔ SUNG: CÁC TRƯỜNG THÔNG TIN CHI TIẾT RIÊNG CHO TỪNG SẢN PHẨM
    // ==============================================================
    @Column(columnDefinition = "TEXT") // Dùng TEXT để lưu được đoạn mô tả rất dài
    private String mo_ta;
    
    private String phong_cach;
    private String luu_huong;
    private String toa_huong;
    private String xuat_xu;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "perfume", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PerfumeVariant> variants;

    public Long getId_nuoc_hoa() {
        return this.id_nuoc_hoa;
    }

    public double getGiaBanNumeric() {
        if (gia_ban == null || gia_ban.isEmpty()) {
            return 0.0;
        }
        try {
            String numericString = gia_ban.replaceAll("[^\\d]", "");
            if (numericString.isEmpty()) {
                return 0.0;
            }
            return Double.parseDouble(numericString);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}