package com.haan.perfumeshop.repository;

import com.haan.perfumeshop.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Tìm đơn hàng theo ID khách hàng
    List<Order> findByUser_Id_user(Long idUser);

    // ======================================================
    // THÊM 2 HÀM NÀY ĐỂ PHỤC VỤ CHO DASHBOARD
    // ======================================================

    // 1. Đếm số lượng đơn hàng đã giao thành công
    @Query("SELECT COUNT(o) FROM Order o WHERE o.trang_thai = 'Delivered'")
    long countDeliveredOrders();

    // 2. Tính tổng doanh thu (Bỏ qua các đơn đã bị Hủy - Cancelled)
    // Dùng COALESCE để nếu chưa có đơn nào thì trả về 0 thay vì bị lỗi null
    @Query("SELECT COALESCE(SUM(o.tong_tien), 0) FROM Order o WHERE o.trang_thai != 'Cancelled'")
    Double calculateTotalRevenue();
}