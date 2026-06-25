package com.haan.perfumeshop.repository;

import com.haan.perfumeshop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Thêm hàm này để hệ thống biết cách tìm người dùng bằng Email khi đăng nhập
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u ORDER BY u.id_user ASC")
    Page<User> findAllUsersSorted(Pageable pageable);
}