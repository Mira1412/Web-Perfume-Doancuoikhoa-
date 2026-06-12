package com.haan.perfumeshop.model;

import jakarta.persistence.*;

@Entity
@Table(name = "perfume_variant")
public class PerfumeVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_bien_the;

    private String dung_tich; // Ví dụ: 50ml, 100ml
    private Long gia_ban;     // Giá riêng cho dung tích này
    private Integer ton_kho;  // Số lượng kho riêng cho dung tích này

    // Liên kết ngược lại với chai nước hoa gốc
    @ManyToOne
    @JoinColumn(name = "id_nuoc_hoa")
    private Perfume perfume;

    // --- GETTERS VÀ SETTERS ---
    public Long getId_bien_the() { return id_bien_the; }
    public void setId_bien_the(Long id_bien_the) { this.id_bien_the = id_bien_the; }

    public String getDung_tich() { return dung_tich; }
    public void setDung_tich(String dung_tich) { this.dung_tich = dung_tich; }

    public Long getGia_ban() { return gia_ban; }
    public void setGia_ban(Long gia_ban) { this.gia_ban = gia_ban; }

    public Integer getTon_kho() { return ton_kho; }
    public void setTon_kho(Integer ton_kho) { this.ton_kho = ton_kho; }

    public Perfume getPerfume() { return perfume; }
    public void setPerfume(Perfume perfume) { this.perfume = perfume; }
}