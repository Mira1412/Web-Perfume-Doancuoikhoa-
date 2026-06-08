package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.repository.PerfumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductController {

    @Autowired
    private PerfumeRepository perfumeRepository;

    @GetMapping("/product/{id}")
    public String showProductDetail(@PathVariable("id") Long id, Model model) {
        // Tìm kiếm sản phẩm theo ID, nếu không thấy thì báo lỗi
        Perfume perfume = perfumeRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy nước hoa này!"));
        model.addAttribute("perfume", perfume);
        return "detail"; // Tên file HTML bạn sẽ tạo ở bước 2
    }
}