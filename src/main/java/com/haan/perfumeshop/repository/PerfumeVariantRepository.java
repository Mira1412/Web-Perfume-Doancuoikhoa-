package com.haan.perfumeshop.repository;

import com.haan.perfumeshop.model.PerfumeVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfumeVariantRepository extends JpaRepository<PerfumeVariant, Long> {
}