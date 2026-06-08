package com.haan.perfumeshop.repository;

import com.haan.perfumeshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Hàm này giúp Spring Boot tự động tìm user theo cột email trong database
    User findByEmail(String email);
}