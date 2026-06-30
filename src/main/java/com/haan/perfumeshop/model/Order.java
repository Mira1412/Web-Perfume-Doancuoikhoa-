package com.haan.perfumeshop.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_don_hang")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Column(nullable = false)
    private String trang_thai = "Pending"; // Mặc định tự gán là Pending khi vừa đặt

    private LocalDateTime ngay_dat;

    private Double tong_tien = 0.0;

    // Phương thức thanh toán: "COD" hoặc "VNPay"
    @Column(name = "phuong_thuc_thanh_toan")
    private String phuong_thuc_thanh_toan = "COD";

    // Mã giao dịch VNPay (chỉ có khi thanh toán qua VNPay)
    @Column(name = "ma_giao_dich")
    private String ma_giao_dich;

    @PrePersist
    protected void onCreate() {
        this.ngay_dat = LocalDateTime.now();
    }
}