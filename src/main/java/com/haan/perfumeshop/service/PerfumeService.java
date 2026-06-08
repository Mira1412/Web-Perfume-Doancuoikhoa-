package com.haan.perfumeshop.service;

import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.repository.PerfumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PerfumeService {

    @Autowired
    private PerfumeRepository perfumeRepository;

    // Lấy danh sách toàn bộ nước hoa hiển thị lên trang chủ
    public List<Perfume> getAllPerfumes() {
        return perfumeRepository.findAll();
    }

    // Lấy thông tin chi tiết của một chai nước hoa khi khách bấm xem
    public Optional<Perfume> getPerfumeById(Long id) {
        return perfumeRepository.findById(id);
    }

    // Lưu hoặc cập nhật sản phẩm (Dành cho chức năng quản lý của Admin)
    public Perfume savePerfume(Perfume perfume) {
        return perfumeRepository.save(perfume);
    }

    // Xóa sản phẩm (Dành cho Admin)
    public void deletePerfume(Long id) {
        perfumeRepository.deleteById(id);
    }
}