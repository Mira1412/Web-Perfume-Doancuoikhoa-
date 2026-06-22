package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.repository.PerfumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private PerfumeRepository perfumeRepository;

    // Đường dẫn thư mục lưu ảnh cục bộ trong dự án
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    // ==========================================
    // 1. DANH SÁCH SẢN PHẨM (trang admin/products)
    // ==========================================
    @GetMapping
    public String listProducts(Model model) {
        List<Perfume> perfumes = perfumeRepository.findAll();
        model.addAttribute("perfumes", perfumes); // Template products.html dùng "perfumes"
        return "admin/products";
    }

    // ==========================================
    // 2. FORM THÊM SẢN PHẨM MỚI (trang admin/add-product)
    // ==========================================
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("perfume", new Perfume());
        return "admin/add-product"; // Dùng template add-product.html có sẵn của đồ án
    }

    // ==========================================
    // 3. FORM CHỈNH SỬA SẢN PHẨM (trang admin/edit-product)
    // ==========================================
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Perfume perfume = perfumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID sản phẩm không hợp lệ: " + id));
        model.addAttribute("perfume", perfume);
        return "admin/edit-product"; // Dùng template edit-product.html có sẵn của đồ án
    }

    // ==========================================
    // 4. XỬ LÝ LƯU SẢN PHẨM MỚI (POST từ add-product.html)
    // ==========================================
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("perfume") Perfume perfume,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              RedirectAttributes redirectAttributes) {
        try {
            // Xử lý upload ảnh nếu người dùng có chọn file
            if (imageFile != null && !imageFile.isEmpty()) {
                String savedPath = uploadImage(imageFile);
                perfume.setHinh_anh(savedPath);
            }

            perfumeRepository.save(perfume);
            redirectAttributes.addFlashAttribute("successMsg", "Thêm sản phẩm thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi khi tải ảnh lên: " + e.getMessage());
            return "redirect:/admin/products/add";
        }

        return "redirect:/admin/products";
    }

    // ==========================================
    // 5. XỬ LÝ CẬP NHẬT SẢN PHẨM (POST từ edit-product.html)
    // ==========================================
    @PostMapping("/update")
    public String updateProduct(@ModelAttribute("perfume") Perfume perfume,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                RedirectAttributes redirectAttributes) {
        try {
            // Xử lý upload ảnh mới nếu người dùng có chọn file
            if (imageFile != null && !imageFile.isEmpty()) {
                String savedPath = uploadImage(imageFile);
                perfume.setHinh_anh(savedPath);
            } else if (perfume.getId_nuoc_hoa() != null) {
                // Nếu không chọn ảnh mới → giữ nguyên ảnh cũ từ DB
                Perfume oldPerfume = perfumeRepository.findById(perfume.getId_nuoc_hoa()).orElse(null);
                if (oldPerfume != null) {
                    perfume.setHinh_anh(oldPerfume.getHinh_anh());
                }
            }

            perfumeRepository.save(perfume);
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật sản phẩm thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi khi tải ảnh lên: " + e.getMessage());
            return "redirect:/admin/products/edit/" + perfume.getId_nuoc_hoa();
        }

        return "redirect:/admin/products";
    }

    // ==========================================
    // 6. XỬ LÝ XÓA SẢN PHẨM
    // ==========================================
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            perfumeRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMsg", "Xóa sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Không thể xóa sản phẩm này (có thể sản phẩm đang nằm trong đơn hàng của khách).");
        }
        return "redirect:/admin/products";
    }

    // ==========================================
    // HÀM TIỆN ÍCH: Upload file ảnh lên thư mục /uploads/
    // ==========================================
    private String uploadImage(MultipartFile imageFile) throws IOException {
        // Tạo thư mục lưu trữ nếu chưa tồn tại
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Đổi tên file bằng chuỗi UUID ngẫu nhiên để tránh trùng tên file ảnh
        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // Lưu file vật lý vào ổ cứng
        Files.copy(imageFile.getInputStream(), filePath);

        // Trả về đường dẫn tương đối để lưu vào database và hiển thị lên giao diện
        return "/uploads/" + fileName;
    }
}