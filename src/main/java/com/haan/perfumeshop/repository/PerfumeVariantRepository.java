package com.haan.perfumeshop.repository;

import com.haan.perfumeshop.model.PerfumeVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PerfumeVariantRepository extends JpaRepository<PerfumeVariant, Long> {

    // Viết câu lệnh SQL trực tiếp để tìm biến thể theo ID nước hoa
    @Query("SELECT v FROM PerfumeVariant v WHERE v.perfume.id_nuoc_hoa = :perfumeId")
    List<PerfumeVariant> findByPerfumeId(@Param("perfumeId") Long perfumeId);
}