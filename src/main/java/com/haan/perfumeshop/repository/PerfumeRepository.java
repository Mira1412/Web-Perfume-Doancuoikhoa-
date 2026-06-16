package com.haan.perfumeshop.repository;

import com.haan.perfumeshop.model.Perfume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PerfumeRepository extends JpaRepository<Perfume, Long>, JpaSpecificationExecutor<Perfume> {

    // Lấy danh sách các thương hiệu duy nhất để hiển thị trong dropdown bộ lọc
    @Query("SELECT DISTINCT p.thuong_hieu FROM Perfume p WHERE p.thuong_hieu IS NOT NULL ORDER BY p.thuong_hieu")
    List<String> findDistinctThuongHieu();

    // Lấy danh sách các nhóm hương duy nhất để hiển thị trong dropdown bộ lọc
    @Query("SELECT DISTINCT p.nhom_huong FROM Perfume p WHERE p.nhom_huong IS NOT NULL ORDER BY p.nhom_huong")
    List<String> findDistinctNhomHuong();

    // BỔ SUNG HÀM TÌM KIẾM
    // LOWER() giúp tìm kiếm không phân biệt chữ hoa, chữ thường
    @Query("SELECT p FROM Perfume p WHERE LOWER(p.ten_sp) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.thuong_hieu) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Perfume> searchPerfumes(@org.springframework.data.repository.query.Param("keyword") String keyword);
}