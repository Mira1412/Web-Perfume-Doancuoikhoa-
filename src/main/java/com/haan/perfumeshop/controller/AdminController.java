package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Order;
import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.model.PerfumeVariant;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.repository.OrderRepository;
import com.haan.perfumeshop.repository.PerfumeRepository;
import com.haan.perfumeshop.repository.PerfumeVariantRepository;
import com.haan.perfumeshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private PerfumeRepository perfumeRepository;

    @Autowired
    private PerfumeVariantRepository variantRepository;

    @Autowired
    private OrderRepository orderRepository; // Đã thêm OrderRepository

    @Autowired
    private UserRepository userRepository;

    // ==========================================
    // 1. DASHBOARD & ĐIỀU HƯỚNG GỐC
    // ==========================================
    @GetMapping({ "", "/" })
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 1. Đếm tổng số lượng sản phẩm thực tế từ Database
        long totalProducts = perfumeRepository.count();
        
        // 2. Lấy các con số thống kê Đơn hàng từ OrderRepository
        long totalOrders = orderRepository.count(); // Tổng số mọi đơn hàng
        long deliveredOrders = orderRepository.countDeliveredOrders(); // Chỉ đếm đơn "Delivered"
        Double totalRevenue = orderRepository.calculateTotalRevenue(); // Tổng tiền thu được
        if (totalRevenue == null) {
            totalRevenue = 0.0;
        }

        // Thống kê doanh thu theo tháng
        List<Order> orders = orderRepository.findAll();
        java.util.Map<String, Double> monthlyRevenue = new java.util.TreeMap<>();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MM/yyyy");
        for (Order o : orders) {
            if (!"Cancelled".equalsIgnoreCase(o.getTrang_thai()) && o.getNgay_dat() != null) {
                String monthKey = "Tháng " + o.getNgay_dat().format(formatter);
                monthlyRevenue.put(monthKey, monthlyRevenue.getOrDefault(monthKey, 0.0) + o.getTong_tien());
            }
        }
        
        List<String> months = new java.util.ArrayList<>(monthlyRevenue.keySet());
        List<Double> revenues = new java.util.ArrayList<>(monthlyRevenue.values());

        // Nếu chưa có dữ liệu doanh thu, hiển thị 6 tháng gần nhất với giá trị 0
        if (months.isEmpty()) {
            java.time.LocalDate now = java.time.LocalDate.now();
            java.time.format.DateTimeFormatter mFormatter = java.time.format.DateTimeFormatter.ofPattern("MM/yyyy");
            for (int i = 5; i >= 0; i--) {
                months.add("Tháng " + now.minusMonths(i).format(mFormatter));
                revenues.add(0.0);
            }
        }

        // 3. Truyền dữ liệu ra màn hình HTML
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("chartMonths", months);
        model.addAttribute("chartRevenues", revenues);

        return "admin/admin-dashboard";
    }
    // ==========================================
    // 2. QUẢN LÝ SẢN PHẨM (NƯỚC HOA)
    // ==========================================
    // === ĐÃ COMMENT LẠI ĐỂ CHUYỂN SANG CONTROLLER MỚI CÓ HỖ TRỢ TÌM KIẾM/LỌC ===
    // @GetMapping("/products")
    // public String listProducts(Model model) {
    //     List<Perfume> perfumes = perfumeRepository.findAll();
    //     model.addAttribute("perfumes", perfumes);
    //     return "admin/products";
    // }

    // === CÁC HÀM BÊN DƯỚI ĐÃ ĐƯỢC CHUYỂN SANG AdminProductController ===
    // @GetMapping("/products/add")
    // public String showAddProductForm(Model model) {
    //     model.addAttribute("perfume", new Perfume());
    //     return "admin/add-product";
    // }

    // @PostMapping("/products/save")
    // public String saveProduct(@ModelAttribute("perfume") Perfume perfume,
    //         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
    //     perfumeRepository.save(perfume);
    //     return "redirect:/admin/products";
    // }

    // @GetMapping("/products/edit/{id}")
    // public String showEditProductForm(@PathVariable("id") Long id, Model model) {
    //     Perfume perfume = perfumeRepository.findById(id)
    //             .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
    //     model.addAttribute("perfume", perfume);
    //     return "admin/edit-product";
    // }

    // @PostMapping("/products/update")
    // public String updateProduct(@ModelAttribute("perfume") Perfume perfume,
    //         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
    //     perfumeRepository.save(perfume);
    //     return "redirect:/admin/products";
    // }

    // @GetMapping("/products/delete/{id}")
    // public String deleteProduct(@PathVariable("id") Long id) {
    //     perfumeRepository.deleteById(id);
    //     return "redirect:/admin/products";
    // }

    // ==========================================
    // 3. QUẢN LÝ BIẾN THỂ (DUNG TÍCH & GIÁ)
    // ==========================================
    @GetMapping("/products/{id}/variants")
    public String showProductVariants(@PathVariable("id") Long id, Model model) {
        Perfume perfume = perfumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

        List<PerfumeVariant> variants = variantRepository.findByPerfumeId(id);

        model.addAttribute("perfume", perfume);
        model.addAttribute("variants", variants);
        model.addAttribute("newVariant", new PerfumeVariant());

        return "admin/variants-manager";
    }

    @PostMapping("/products/{id}/variants/add")
    public String addVariant(@PathVariable("id") Long id,
            @ModelAttribute("newVariant") PerfumeVariant variant) {
        Perfume perfume = perfumeRepository.findById(id).orElse(null);
        if (perfume != null) {
            variant.setPerfume(perfume);
            variantRepository.save(variant);
        }
        return "redirect:/admin/products/" + id + "/variants";
    }

    @GetMapping("/variants/delete/{variantId}")
    public String deleteVariant(@PathVariable("variantId") Long variantId) {
        PerfumeVariant variant = variantRepository.findById(variantId).orElse(null);
        Long perfumeId = null;
        if (variant != null) {
            perfumeId = variant.getPerfume().getId_nuoc_hoa();
            variantRepository.delete(variant);
        }
        return "redirect:/admin/products/" + perfumeId + "/variants";
    }

    // ==========================================
    // 4. QUẢN LÝ ĐƠN HÀNG TRANG ADMIN
    // ==========================================
    @GetMapping("/orders")
    public String listOrders(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        if (size <= 0) size = 10;
        if (page < 0) page = 0;

        Page<Order> orderPage = orderRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", orderPage.getNumber());
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());
        model.addAttribute("pageSize", size);
        return "admin/admin-orders";
    }

    // ==========================================
    // 5. QUẢN LÝ NGƯỜI DÙNG
    // ==========================================
    @GetMapping("/users")
    public String listUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        if (size <= 0) size = 10;
        if (page < 0) page = 0;

        Page<User> userPage = userRepository.findAllUsersSorted(
                PageRequest.of(page, size));

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("pageSize", size);
        return "admin/users";
    }

    @PostMapping("/orders/update-status")
    public String updateOrderStatus(@RequestParam("id") Long id, @RequestParam("status") String status) {
        // Tìm đơn hàng theo ID
        Order order = orderRepository.findById(id).orElse(null);

        if (order != null) {
            order.setTrang_thai(status); // Cập nhật trạng thái mới
            orderRepository.save(order); // Lưu vào Database
        }

        return "redirect:/admin/orders"; // Quay lại trang quản lý đơn hàng để thấy kết quả
    }

    @PostMapping("/users/update-role")
    public String updateUserRole(@RequestParam("id") Long id, @RequestParam("role") String role) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setRole(role);
            userRepository.save(user);
        }
        return "redirect:/admin/users";
    }
}