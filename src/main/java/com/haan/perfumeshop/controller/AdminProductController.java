package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.model.PerfumeVariant;
import com.haan.perfumeshop.repository.PerfumeRepository;
import com.haan.perfumeshop.repository.PerfumeVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Autowired
    private PerfumeVariantRepository variantRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    private static final int DEFAULT_PAGE_SIZE = 10;

    // ==========================================
    // 1. DANH SÁCH SẢN PHẨM (CÓ PHÂN TRANG)
    // ==========================================
    @GetMapping
    public String listProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        if (size <= 0) size = DEFAULT_PAGE_SIZE;
        if (page < 0) page = 0;

        Page<Perfume> perfumePage = perfumeRepository.findAll(
                PageRequest.of(page, size));

        model.addAttribute("perfumes", perfumePage.getContent());
        model.addAttribute("currentPage", perfumePage.getNumber());
        model.addAttribute("totalPages", perfumePage.getTotalPages());
        model.addAttribute("totalItems", perfumePage.getTotalElements());
        model.addAttribute("pageSize", size);
        return "admin/products";
    }

    // ==========================================
    // 2. FORM THÊM SẢN PHẨM MỚI
    // ==========================================
    @GetMapping("/add")
    public String showAddForm(Model model) {
        Perfume perfume = new Perfume();
        // Tạo sẵn 1 dòng biến thể trống để admin nhập liệu
        perfume.getVariants().add(new PerfumeVariant());

        model.addAttribute("perfume", perfume);
        model.addAttribute("isEdit", false);
        model.addAttribute("title", "Thêm Sản Phẩm Mới");
        return "admin/product-form";
    }

    // ==========================================
    // 3. FORM CHỈNH SỬA SẢN PHẨM
    // ==========================================
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Perfume perfume = perfumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID sản phẩm không hợp lệ: " + id));

        // Nếu sản phẩm chưa có biến thể nào, thêm 1 dòng trống để admin thêm mới
        if (perfume.getVariants().isEmpty()) {
            perfume.getVariants().add(new PerfumeVariant());
        }

        model.addAttribute("perfume", perfume);
        model.addAttribute("isEdit", true);
        model.addAttribute("title", "Chỉnh Sửa: " + perfume.getTen_sp());
        return "admin/product-form";
    }

    // ==========================================
    // 4. LƯU SẢN PHẨM MỚI (POST /save)
    // ==========================================
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("perfume") Perfume perfume,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              RedirectAttributes redirectAttributes) {
        try {
            // Xử lý upload ảnh
            if (imageFile != null && !imageFile.isEmpty()) {
                perfume.setHinh_anh(uploadImage(imageFile));
            }

            // Móc nối biến thể vào sản phẩm cha
            if (perfume.getVariants() != null) {
                perfume.getVariants().removeIf(v ->
                        v == null ||
                        ((v.getDung_tich() == null || v.getDung_tich().isBlank()) &&
                        (v.getGia_ban() == null || v.getGia_ban().isBlank()))
                );
                for (PerfumeVariant variant : perfume.getVariants()) {
                    if (variant != null) {
                        variant.setPerfume(perfume);
                    }
                }
            }

            perfumeRepository.save(perfume);
            redirectAttributes.addFlashAttribute("successMsg", "Thêm sản phẩm thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi khi tải ảnh: " + e.getMessage());
            return "redirect:/admin/products/add";
        }
        return "redirect:/admin/products";
    }

    // ==========================================
    // 5. CẬP NHẬT SẢN PHẨM (POST /update)
    // ==========================================
    @PostMapping("/update")
    public String updateProduct(@ModelAttribute("perfume") Perfume formPerfume,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                RedirectAttributes redirectAttributes) {
        Long id = formPerfume.getId_nuoc_hoa();
        System.out.println("=== UPDATE PRODUCT DEBUG ===");
        System.out.println("Product ID: " + id);
        if (formPerfume.getVariants() != null) {
            System.out.println("Form Variants Size: " + formPerfume.getVariants().size());
            for (int i = 0; i < formPerfume.getVariants().size(); i++) {
                PerfumeVariant v = formPerfume.getVariants().get(i);
                System.out.println("Variant[" + i + "]: " + (v == null ? "null" : (v.getDung_tich() + " - " + v.getGia_ban() + " - " + v.getSo_luong_ton())));
            }
        } else {
            System.out.println("Form Variants is NULL");
        }

        if (id == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "Không tìm thấy ID sản phẩm cần cập nhật.");
            return "redirect:/admin/products";
        }

        try {
            // Tải sản phẩm gốc từ DB để giữ lại dữ liệu quan trọng
            Perfume existingPerfume = perfumeRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại: " + id));

            // --- Cập nhật các trường thông tin cơ bản ---
            existingPerfume.setTen_sp(formPerfume.getTen_sp());
            existingPerfume.setThuong_hieu(formPerfume.getThuong_hieu());
            existingPerfume.setNhom_huong(formPerfume.getNhom_huong());
            existingPerfume.setGioi_tinh(formPerfume.getGioi_tinh());
            existingPerfume.setXuat_xu(formPerfume.getXuat_xu());
            existingPerfume.setPhong_cach(formPerfume.getPhong_cach());
            existingPerfume.setLuu_huong(formPerfume.getLuu_huong());
            existingPerfume.setToa_huong(formPerfume.getToa_huong());
            existingPerfume.setMo_ta(formPerfume.getMo_ta());

            // --- Xử lý ảnh: chỉ thay khi admin chọn file mới ---
            if (imageFile != null && !imageFile.isEmpty()) {
                existingPerfume.setHinh_anh(uploadImage(imageFile));
            }
            // Nếu để trống → giữ nguyên ảnh cũ (không làm gì)

            // --- Cập nhật danh sách biến thể ---
            List<PerfumeVariant> existingVariants = existingPerfume.getVariants();
            List<PerfumeVariant> updatedVariants = new java.util.ArrayList<>();

            if (formPerfume.getVariants() != null) {
                for (PerfumeVariant formVariant : formPerfume.getVariants()) {
                    if (formVariant == null) {
                        continue;
                    }
                    // Bỏ qua dòng trống hoàn toàn
                    if ((formVariant.getDung_tich() == null || formVariant.getDung_tich().isBlank()) &&
                        (formVariant.getGia_ban() == null || formVariant.getGia_ban().isBlank())) {
                        continue;
                    }

                    if (formVariant.getId_bien_the() != null) {
                        // Tìm biến thể cũ tương ứng trong DB để cập nhật dữ liệu
                        PerfumeVariant matchedVariant = existingVariants.stream()
                                .filter(ev -> ev.getId_bien_the().equals(formVariant.getId_bien_the()))
                                .findFirst()
                                .orElse(null);

                        if (matchedVariant != null) {
                            matchedVariant.setDung_tich(formVariant.getDung_tich());
                            matchedVariant.setGia_ban(formVariant.getGia_ban());
                            matchedVariant.setSo_luong_ton(formVariant.getSo_luong_ton());
                            updatedVariants.add(matchedVariant);
                        } else {
                            formVariant.setId_bien_the(null);
                            formVariant.setPerfume(existingPerfume);
                            updatedVariants.add(formVariant);
                        }
                    } else {
                        formVariant.setPerfume(existingPerfume);
                        updatedVariants.add(formVariant);
                    }
                }
            }

            // Xóa các biến thể không còn tồn tại trong form
            existingVariants.removeIf(ev -> !updatedVariants.contains(ev));

            // Thêm các biến thể mới vào list hiện tại
            for (PerfumeVariant uv : updatedVariants) {
                if (!existingVariants.contains(uv)) {
                    existingVariants.add(uv);
                }
            }

            perfumeRepository.save(existingPerfume);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Cập nhật sản phẩm \"" + existingPerfume.getTen_sp() + "\" thành công!");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi khi tải ảnh: " + e.getMessage());
            return "redirect:/admin/products/edit/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi khi cập nhật: " + e.getMessage());
            return "redirect:/admin/products/edit/" + id;
        }

        return "redirect:/admin/products";
    }

    // ==========================================
    // 6. XÓA SẢN PHẨM
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
    // TIỆN ÍCH: Upload ảnh lên /uploads/
    // ==========================================
    private String uploadImage(MultipartFile imageFile) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(imageFile.getInputStream(), filePath);

        // Trả về đường dẫn tương đối để lưu vào database và hiển thị lên giao diện
        return "/uploads/" + fileName;
    }
}