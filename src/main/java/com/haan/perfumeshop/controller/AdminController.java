package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Order;
import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.model.PerfumeVariant;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.repository.OrderRepository;
import com.haan.perfumeshop.repository.OrderDetailRepository;
import com.haan.perfumeshop.repository.PerfumeRepository;
import com.haan.perfumeshop.repository.PerfumeVariantRepository;
import com.haan.perfumeshop.repository.UserRepository;
import com.haan.perfumeshop.service.ExportService;
import com.haan.perfumeshop.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


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

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ExportService exportService;

    @Autowired
    private EmailService emailService;


    // ==========================================
    // 1. DASHBOARD & ĐIỀU HƯỚNG GỐC
    // ==========================================
    @GetMapping({ "", "/", "/dashboarb" })
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 1. Thống kê cơ bản
        long totalProducts = perfumeRepository.count();
        long totalOrders   = orderRepository.count();
        long deliveredOrders = orderRepository.countDeliveredOrders();
        long pendingOrders   = orderRepository.countPendingOrders();
        long totalUsers      = userRepository.count();
        Double totalRevenue  = orderRepository.calculateTotalRevenue();
        if (totalRevenue == null) totalRevenue = 0.0;

        // 2. Doanh thu theo tháng
        List<Order> allOrders = orderRepository.findAll();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("MM/yyyy");
        Map<String, Double>  monthlyRevenue = new TreeMap<>();
        Map<String, Integer> monthlyOrders  = new TreeMap<>();
        for (Order o : allOrders) {
            if (o.getNgay_dat() == null) continue;
            String key = "Tháng " + o.getNgay_dat().format(fmt);
            if (!"Cancelled".equalsIgnoreCase(o.getTrang_thai())) {
                monthlyRevenue.put(key, monthlyRevenue.getOrDefault(key, 0.0) + o.getTong_tien());
            }
            monthlyOrders.put(key, monthlyOrders.getOrDefault(key, 0) + 1);
        }
        List<String> months   = new ArrayList<>(monthlyRevenue.keySet());
        List<Double> revenues = new ArrayList<>(monthlyRevenue.values());
        List<Integer> orderCounts = new ArrayList<>();
        for (String m : months) orderCounts.add(monthlyOrders.getOrDefault(m, 0));

        // Nếu chưa có dữ liệu, hiển thị 6 tháng gần nhất với giá trị 0
        if (months.isEmpty()) {
            java.time.LocalDate now = java.time.LocalDate.now();
            for (int i = 5; i >= 0; i--) {
                months.add("Tháng " + now.minusMonths(i).format(fmt));
                revenues.add(0.0);
                orderCounts.add(0);
            }
        }

        // 3. 7 đơn hàng mới nhất
        List<Order> recentOrders = orderRepository.findTop7ByOrderByNgayDatDesc(
                PageRequest.of(0, 7));

        // 4. Top 5 sản phẩm bán chạy
        List<Object[]> topRaw = orderDetailRepository.findTopSellingProducts(PageRequest.of(0, 5));
        List<Map<String, Object>> topProducts = new ArrayList<>();
        long maxSold = topRaw.isEmpty() ? 1 : ((Number) topRaw.get(0)[1]).longValue();
        for (Object[] row : topRaw) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name",     row[0]);
            item.put("sold",     ((Number) row[1]).longValue());
            item.put("percent", maxSold > 0 ? (int)(((Number) row[1]).longValue() * 100 / maxSold) : 0);
            topProducts.add(item);
        }

        // 5. Cảnh báo sản phẩm sắp hết hàng (tồn kho < 5)
        List<PerfumeVariant> lowStockVariants = variantRepository.findAll();
        List<PerfumeVariant> lowStockList = new ArrayList<>();
        for (PerfumeVariant v : lowStockVariants) {
            if (v.getSo_luong_ton() != null && v.getSo_luong_ton() < 5) {
                lowStockList.add(v);
            }
        }

        // 6. Truyền tất cả ra View
        model.addAttribute("totalProducts",   totalProducts);
        model.addAttribute("totalOrders",     totalOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("pendingOrders",   pendingOrders);
        model.addAttribute("totalUsers",      totalUsers);
        model.addAttribute("totalRevenue",    totalRevenue);
        model.addAttribute("chartMonths",     months);
        model.addAttribute("chartRevenues",   revenues);
        model.addAttribute("chartOrders",     orderCounts);
        model.addAttribute("recentOrders",    recentOrders);
        model.addAttribute("topProducts",     topProducts);
        model.addAttribute("lowStockList",    lowStockList);

        return "admin/admin-dashboard";
    }
    
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

    @GetMapping("/orders/export")
    public ResponseEntity<byte[]> exportOrders() {
        try {
            List<Order> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            byte[] excelData = exportService.exportOrdersToExcel(orders);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "Danh_sach_don_hang.xlsx");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
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
            String oldStatus = order.getTrang_thai();
            order.setTrang_thai(status); // Cập nhật trạng thái mới
            orderRepository.save(order); // Lưu vào Database

            // Nếu trạng thái thay đổi thì gửi mail thông báo cập nhật
            if (!status.equalsIgnoreCase(oldStatus)) {
                try {
                    emailService.sendOrderStatusUpdateEmail(order);
                } catch (Exception e) {
                    // Không block luồng admin nếu gửi mail lỗi
                    System.out.println("⚠️ Lỗi gửi email thông báo cập nhật đơn hàng: " + e.getMessage());
                }
            }
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