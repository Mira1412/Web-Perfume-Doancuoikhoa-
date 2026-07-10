package com.haan.perfumeshop.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "wishlists", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_user", "id_nuoc_hoa"})
})
@Data
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_nuoc_hoa", nullable = false)
    private Perfume perfume;

    @Column(name = "ngay_them")
    private LocalDateTime ngayThem;

    @PrePersist
    protected void onCreate() {
        this.ngayThem = LocalDateTime.now();
    }
}
