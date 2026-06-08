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

    @Column(nullable = false)
    private Integer so_luong_mua;

    @Column(nullable = false)
    private Double gia_luc_mua;
}