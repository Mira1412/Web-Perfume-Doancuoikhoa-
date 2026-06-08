package com.haan.perfumeshop.repository;

import com.haan.perfumeshop.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Sử dụng @Query để chỉ định rõ cột cần tìm, tránh lỗi tự động dịch sai của Spring Boot
    @Query("SELECT o FROM Order o WHERE o.user.id_user = :idUser")
    List<Order> findByUser_Id_user(@Param("idUser") Long idUser);
    
}