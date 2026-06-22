package com.haan.perfumeshop.model;

import jakarta.persistence.*;

@Entity
@Table(name = "perfume_variant")
public class PerfumeVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_bien_the;

    private String dung_tich;
    private String gia_ban; // Dùng String hoặc Double tùy theo kiểu cũ bạn đang thiết lập
    private Integer so_luong_ton = 100; // Số lượng mặc định

    // Nối ngược lại với bảng Nước hoa gốc (Nhiều Biến thể thuộc về 1 Nước hoa)
    @ManyToOne
    @JoinColumn(name = "id_nuoc_hoa")
    private Perfume perfume;

    // --- GETTER & SETTER ---
    public Long getId_bien_the() {
        return id_bien_the;
    }

    public void setId_bien_the(Long id_bien_the) {
        this.id_bien_the = id_bien_the;
    }

    public String getDung_tich() {
        return dung_tich;
    }

    public void setDung_tich(String dung_tich) {
        this.dung_tich = dung_tich;
    }

    public String getGia_ban() {
        return gia_ban;
    }

    public void setGia_ban(String gia_ban) {
        this.gia_ban = gia_ban;
    }

    public Integer getSo_luong_ton() {
        return so_luong_ton;
    }

    public void setSo_luong_ton(Integer so_luong_ton) {
        this.so_luong_ton = so_luong_ton;
    }

    public Perfume getPerfume() {
        return perfume;
    }

    public void setPerfume(Perfume perfume) {
        this.perfume = perfume;
    }
}