package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.service.PerfumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/perfumes")
@CrossOrigin(origins = "*") // Cho phép tất cả các ứng dụng Frontend gọi API này
public class PerfumeController {

    @Autowired
    private PerfumeService perfumeService;

    // API lấy toàn bộ nước hoa: GET http://localhost:8080/api/perfumes
    @GetMapping
    public ResponseEntity<List<Perfume>> getAllPerfumes() {
        return ResponseEntity.ok(perfumeService.getAllPerfumes());
    }

    // API thêm một sản phẩm mới: POST http://localhost:8080/api/perfumes
    @PostMapping
    public ResponseEntity<Perfume> createPerfume(@RequestBody Perfume perfume) {
        return ResponseEntity.ok(perfumeService.savePerfume(perfume));
    }
}