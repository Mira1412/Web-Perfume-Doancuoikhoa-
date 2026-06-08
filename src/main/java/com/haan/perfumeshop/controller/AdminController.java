package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.repository.PerfumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private PerfumeRepository perfumeRepository;

    // Hiển thị danh sách sản phẩm cho Admin quản lý
    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("perfumes", perfumeRepository.findAll());
        return "admin/products"; // File nằm trong templates/admin/products.html
    }

    // Xóa sản phẩm
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id) {
        perfumeRepository.deleteById(id);
        return "redirect:/admin/products";
    }
}