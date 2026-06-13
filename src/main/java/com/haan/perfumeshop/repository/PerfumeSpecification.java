package com.haan.perfumeshop.repository;

import com.haan.perfumeshop.model.Perfume;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PerfumeSpecification {

    /**
     * Tạo Specification lọc động theo thương hiệu và nhóm hương.
     * (Giá lọc ở tầng Java sau vì gia_ban là String)
     *
     * @param thuongHieu Tên thương hiệu cần lọc (null = bỏ qua)
     * @param nhomHuong  Nhóm hương cần lọc (null = bỏ qua)
     * @return Specification kết hợp các điều kiện lọc
     */
    public static Specification<Perfume> filter(String thuongHieu, String nhomHuong) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo thương hiệu (không phân biệt chữ hoa/thường)
            if (thuongHieu != null && !thuongHieu.trim().isEmpty()) {
                predicates.add(cb.equal(
                    cb.lower(root.get("thuong_hieu")),
                    thuongHieu.trim().toLowerCase()
                ));
            }

            // Lọc theo nhóm hương (không phân biệt chữ hoa/thường)
            if (nhomHuong != null && !nhomHuong.trim().isEmpty()) {
                predicates.add(cb.equal(
                    cb.lower(root.get("nhom_huong")),
                    nhomHuong.trim().toLowerCase()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
