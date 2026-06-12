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
    // Sửa Double thành String để nhập được cả chữ và số, dấu gạch ngang
    private String gia_ban;
    private Integer ton_kho;
    private String hinh_anh;

    public double getGiaBanNumeric() {
        if (gia_ban == null || gia_ban.trim().isEmpty()) {
            return 0.0;
        }
        try {
            String clean = gia_ban.split("-")[0].trim();
            clean = clean.replaceAll("[^0-9]", "");
            if (clean.isEmpty()) return 0.0;
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }

    // ==============================================================
    // BỔ SUNG 2 DÒNG EXCLUDE ĐỂ TRÁNH LỖI VÒNG LẶP VÔ TẬN LÀM SẬP WEB
    // ==============================================================
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "perfume", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PerfumeVariant> variants;

    // Tự viết hàm Get thủ công để sửa lỗi undefined
    public Long getId_nuoc_hoa() {
        return this.id_nuoc_hoa;
    }
}