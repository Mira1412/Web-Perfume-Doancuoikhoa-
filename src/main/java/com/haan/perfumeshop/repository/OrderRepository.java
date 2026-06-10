package com.haan.perfumeshop.repository;

import com.haan.perfumeshop.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 1. Dành cho Khách hàng: Lấy danh sách đơn hàng của riêng họ
    // Sử dụng @Query để chỉ định rõ cột cần tìm, tránh lỗi tự động dịch sai
    @Query("SELECT o FROM Order o WHERE o.user.id_user = :idUser")
    List<Order> findByUser_Id_user(@Param("idUser") Long idUser);

    // 2. Dành cho Admin: Lấy tất cả đơn hàng nhưng sắp xếp Đơn Mới Nhất lên đầu
    @Query("SELECT o FROM Order o ORDER BY o.id DESC")
    List<Order> findAllOrdersDesc();
}