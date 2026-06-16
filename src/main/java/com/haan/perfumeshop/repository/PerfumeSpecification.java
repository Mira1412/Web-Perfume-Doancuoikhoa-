package com.haan.perfumeshop.repository;

import com.haan.perfumeshop.model.Perfume;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class PerfumeSpecification {

    public static Specification<Perfume> filter(String thuongHieu, String nhomHuong) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Đã sửa: Dùng LIKE và LOWER để tìm kiếm bao dung hơn (bỏ qua viết hoa/thường và khoảng trắng dư)
            if (thuongHieu != null && !thuongHieu.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("thuong_hieu")), 
                    "%" + thuongHieu.trim().toLowerCase() + "%"
                ));
            }

            // Đã sửa tương tự cho nhóm hương
            if (nhomHuong != null && !nhomHuong.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("nhom_huong")), 
                    "%" + nhomHuong.trim().toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}