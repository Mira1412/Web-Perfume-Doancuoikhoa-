package com.haan.perfumeshop.model;
import jakarta.persistence.*;
import lombok.Data;

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
    private Double gia_ban;
    private Integer ton_kho;
    private String hinh_anh;

    // Tự viết hàm Get thủ công để sửa lỗi undefined
    public Long getId_nuoc_hoa() {
        return this.id_nuoc_hoa;
    }
}