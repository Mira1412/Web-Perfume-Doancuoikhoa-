package com.haan.perfumeshop.repository;

import com.haan.perfumeshop.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ======================================================
    // SỬ DỤNG @Query ĐỂ TRÁNH LỖI TỰ NHẬN DIỆN TÊN BIẾN
    // ======================================================
    @Query("SELECT o FROM Order o WHERE o.user.id_user = :idUser")
    List<Order> findByUser_Id_userList(@Param("idUser") Long idUser);

    // Phiên bản có phân trang
    @Query("SELECT o FROM Order o WHERE o.user.id_user = :idUser")
    Page<Order> findByUser_Id_userPageable(@Param("idUser") Long idUser, Pageable pageable);

    // ======================================================
    // CÁC HÀM TÍNH TOÁN CHO DASHBOARD
    // ======================================================

    // 1. Đếm số lượng đơn hàng đã giao thành công
    @Query("SELECT COUNT(o) FROM Order o WHERE o.trang_thai = 'Delivered'")
    long countDeliveredOrders();

    // 2. Tính tổng doanh thu (Bỏ qua các đơn đã bị Hủy - Cancelled)
    @Query("SELECT COALESCE(SUM(o.tong_tien), 0) FROM Order o WHERE o.trang_thai != 'Cancelled'")
    Double calculateTotalRevenue();
}