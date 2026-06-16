package com.haan.perfumeshop.repository;

import com.haan.perfumeshop.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    // Tìm toàn bộ chi tiết đơn hàng dựa vào mã đơn (id_don_hang)
    List<OrderDetail> findByOrder_Id(Long idDonHang);

    // Kiểm tra user đã mua sản phẩm này chưa (chỉ tính đơn đã giao thành công)
    @Query("SELECT CASE WHEN COUNT(od) > 0 THEN true ELSE false END FROM OrderDetail od WHERE od.order.user.id_user = :userId AND od.perfume.id_nuoc_hoa = :perfumeId AND od.order.trang_thai = 'Delivered'")
    boolean existsByUserAndPerfumeDelivered(@Param("userId") Long userId, @Param("perfumeId") Long perfumeId);
}