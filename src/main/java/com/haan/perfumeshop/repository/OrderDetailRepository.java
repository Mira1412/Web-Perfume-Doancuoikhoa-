package com.haan.perfumeshop.repository;

import com.haan.perfumeshop.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    // Tìm toàn bộ chi tiết đơn hàng dựa vào mã đơn (id_don_hang)
    List<OrderDetail> findByOrder_Id(Long idDonHang);
}