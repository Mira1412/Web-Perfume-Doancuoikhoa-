package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.repository.PerfumeRepository;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import com.haan.perfumeshop.repository.OrderRepository; // Bổ sung thư viện Order
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // Thư mục lưu ảnh upload (nằm ở gốc project)
    private final String UPLOAD_DIR = "uploads/";

    @Autowired
    private PerfumeRepository perfumeRepository;

    @Autowired
    private OrderRepository orderRepository; // Bổ sung Repository để gọi database đơn hàng

    // 1. Hiển thị danh sách sản phẩm cho Admin quản lý
    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("perfumes", perfumeRepository.findAll());
        return "products"; // File nằm trong templates/products.html
    }

    // 2. Xóa sản phẩm
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id) {
        perfumeRepository.deleteById(id);
        return "redirect:/admin/products";
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ: THÊM SẢN PHẨM MỚI
    // ==========================================

    // 3. Hiển thị form thêm sản phẩm mới
    @GetMapping("/products/add")
    public String showAddForm(Model model) {
        // Gửi một đối tượng Perfume rỗng sang form để hứng dữ liệu người dùng nhập
        model.addAttribute("perfume", new Perfume());
        return "add-product"; // File nằm trong templates/add-product.html
    }

    // 4. Lưu sản phẩm vào database sau khi điền form (có upload ảnh)
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute("perfume") Perfume perfume,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {
        
        // Nếu người dùng có chọn file ảnh thì lưu vào thư mục uploads/
        if (imageFile != null && !imageFile.isEmpty()) {
            // Dùng absolute path để tránh lỗi trên Windows
            Path uploadPath = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Tạo tên file duy nhất để tránh trùng: UUID + tên gốc
            String originalFilename = imageFile.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            
            // Lưu file vào thư mục uploads/
            Path filePath = uploadPath.resolve(uniqueFilename);
            imageFile.transferTo(filePath.toAbsolutePath().toFile());
            
            // Lưu đường dẫn vào database để hiển thị trên web
            perfume.setHinh_anh("/uploads/" + uniqueFilename);
        }
        
        // Lưu dữ liệu vào MySQL
        perfumeRepository.save(perfume);
        // Lưu xong thì quay thẳng về trang danh sách sản phẩm
        return "redirect:/admin/products";
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ: SỬA SẢN PHẨM 
    // ==========================================

    // 5. Hiển thị form Sửa sản phẩm (đã lấy sẵn dữ liệu cũ)
    @GetMapping("/products/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        // Tìm sản phẩm theo ID, nếu không thấy thì báo lỗi
        Perfume perfume = perfumeRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy nước hoa này!"));
        
        // Đẩy dữ liệu sản phẩm cũ sang giao diện
        model.addAttribute("perfume", perfume);
        return "edit-product"; // File nằm trong templates/edit-product.html
    }

    // 6. Xử lý cập nhật sản phẩm vào database (có upload ảnh mới)
    @PostMapping("/products/update")
    public String updateProduct(@ModelAttribute("perfume") Perfume formPerfume,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                Model model) {
        try {
            // Lấy sản phẩm cũ từ DB để cập nhật (tránh lỗi detached entity)
            Perfume perfume = perfumeRepository.findById(formPerfume.getId_nuoc_hoa())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + formPerfume.getId_nuoc_hoa()));
            
            // Cập nhật các trường từ form
            perfume.setTen_sp(formPerfume.getTen_sp());
            perfume.setThuong_hieu(formPerfume.getThuong_hieu());
            perfume.setNhom_huong(formPerfume.getNhom_huong());
            perfume.setDung_tich(formPerfume.getDung_tich());
            perfume.setGia_ban(formPerfume.getGia_ban());
            perfume.setTon_kho(formPerfume.getTon_kho());
            
            // Nếu người dùng chọn ảnh mới thì upload và cập nhật
            if (imageFile != null && !imageFile.isEmpty()) {
                // Dùng absolute path để tránh lỗi trên Windows
                Path uploadPath = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                String originalFilename = imageFile.getOriginalFilename();
                String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
                
                Path filePath = uploadPath.resolve(uniqueFilename);
                imageFile.transferTo(filePath.toAbsolutePath().toFile());
                
                perfume.setHinh_anh("/uploads/" + uniqueFilename);
            }
            // Nếu không chọn ảnh mới → giữ nguyên ảnh cũ (đã có sẵn từ DB)
            
            perfumeRepository.save(perfume);
            return "redirect:/admin/products";
            
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console để debug
            // Quay lại trang edit và hiển thị lỗi
            model.addAttribute("perfume", formPerfume);
            model.addAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
            return "edit-product";
        }
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ: QUẢN LÝ ĐƠN HÀNG
    // ==========================================

    // 7. Hiển thị danh sách tất cả đơn hàng cho Admin
    @GetMapping("/orders")
    public String listOrders(Model model) {
        // Lấy tất cả đơn hàng từ database đẩy sang View
        model.addAttribute("orders", orderRepository.findAllOrdersDesc());
        // Trả về file admin-orders.html để không bị trùng với trang lịch sử đơn hàng của khách
        return "admin-orders"; 
    }

    // 8. Cập nhật trạng thái đơn hàng
    @PostMapping("/orders/update-status")
    public String updateOrderStatus(@RequestParam("id") Long id, @RequestParam("status") String status) {
        // Tìm đơn hàng trong database
        com.haan.perfumeshop.model.Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));
        
        // Cập nhật trạng thái mới và lưu lại
        order.setTrang_thai(status);
        orderRepository.save(order);
        
        return "redirect:/admin/orders";
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ: THỐNG KÊ DASHBOARD
    // ==========================================

    // 9. Hiển thị trang Tổng quan Thống kê
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        List<com.haan.perfumeshop.model.Order> allOrders = orderRepository.findAll();
        
        double totalRevenue = 0; // Tổng doanh thu
        int deliveredOrders = 0; // Đơn giao thành công
        
        // Dùng vòng lặp để tính tổng tiền của các đơn đã giao thành công
        for (com.haan.perfumeshop.model.Order o : allOrders) {
            if ("Delivered".equals(o.getTrang_thai())) {
                totalRevenue += o.getTong_tien();
                deliveredOrders++;
            }
        }
        
        // Gửi các con số thống kê ra ngoài giao diện
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalOrders", allOrders.size());
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("totalProducts", perfumeRepository.count()); // Đếm tổng số nước hoa trong kho
        
        return "admin-dashboard"; // File giao diện sẽ tạo ở bước 2
    }
}