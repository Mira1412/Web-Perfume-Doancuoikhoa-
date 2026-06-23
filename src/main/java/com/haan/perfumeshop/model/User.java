package com.haan.perfumeshop.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_user;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // Họ và tên đầy đủ
    @Column(name = "full_name")
    private String fullName;

    // Số điện thoại
    @Column(name = "phone")
    private String phone;

    // Địa chỉ giao hàng
    @Column(name = "address")
    private String address;

    // Vai trò: "customer" hoặc "admin"
    private String role;

    // Tự viết hàm Get thủ công để sửa lỗi undefined
    public Long getId_user() {
        return this.id_user;
    }
}