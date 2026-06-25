package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.haan.perfumeshop.service.EmailService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService; // Khai báo gọi dịch vụ gửi mail

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // 1. Hiển thị trang Hồ sơ cá nhân
    @GetMapping("/profile")
    public String showProfilePage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login"; // Chưa đăng nhập thì đuổi về trang login
        }

        // Lấy dữ liệu mới nhất từ Database phòng trường hợp vừa cập nhật
        User currentUser = userRepository.findById(loggedInUser.getId_user()).orElse(null);
        model.addAttribute("user", currentUser);
        return "profile";
    }

    // 2. Cập nhật thông tin cá nhân
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null)
            return "redirect:/login";

        User currentUser = userRepository.findById(loggedInUser.getId_user()).orElse(null);
        if (currentUser != null) {
            currentUser.setFullName(fullName);
            currentUser.setPhone(phone);
            currentUser.setAddress(address);

            // Xử lý tải ảnh đại diện lên
            if (avatarFile != null && !avatarFile.isEmpty()) {
                try {
                    String uploadDir = "src/main/resources/static/uploads/";
                    Path uploadPath = Paths.get(uploadDir);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }
                    String fileName = UUID.randomUUID().toString() + "_" + avatarFile.getOriginalFilename();
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(avatarFile.getInputStream(), filePath);

                    currentUser.setAvatar("/uploads/" + fileName);
                } catch (IOException e) {
                    redirectAttributes.addFlashAttribute("errorMsg", "Lỗi tải ảnh đại diện: " + e.getMessage());
                    return "redirect:/profile";
                }
            }

            userRepository.save(currentUser);
            session.setAttribute("loggedInUser", currentUser); // Cập nhật lại session

            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật thông tin thành công!");
        }
        return "redirect:/profile";
    }

    // 3. Đổi mật khẩu
    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null)
            return "redirect:/login";

        User currentUser = userRepository.findById(loggedInUser.getId_user()).orElse(null);

        // Kiểm tra mật khẩu cũ bằng hàm matches
        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Mật khẩu cũ không chính xác!");
            return "redirect:/profile";
        }

        // Kiểm tra mật khẩu mới và xác nhận
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMsg", "Mật khẩu xác nhận không khớp!");
            return "redirect:/profile";
        }

        // Mã hóa mật khẩu mới trước khi lưu
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(currentUser);

        redirectAttributes.addFlashAttribute("successMsg", "Đổi mật khẩu thành công!");
        return "redirect:/profile";
    }

    // ==========================================
    // CHỨC NĂNG ĐĂNG NHẬP / ĐĂNG XUẤT
    // ==========================================

    // 1. Hiển thị trang Login
    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        User existing = (User) session.getAttribute("loggedInUser");
        if (existing != null) {
            // Admin đã đăng nhập → đẩy thẳng vào trang quản trị
            if ("ADMIN".equals(existing.getRole())) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/"; // Khách thường → về trang chủ
        }
        return "login"; // Gọi file login.html
    }

    // 2. Xử lý khi khách bấm nút "Đăng nhập"
    @PostMapping("/login")
    public String processLogin(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Tìm user theo email
        User user = userRepository.findByEmail(email).orElse(null);

        // Kiểm tra xem user có tồn tại và mật khẩu có khớp không (dùng BCrypt)
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // Đăng nhập thành công -> Lưu user vào Session
            session.setAttribute("loggedInUser", user);

            // Nếu là tài khoản ADMIN → đẩy thẳng vào trang quản trị, bỏ qua trang chủ
            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/"; // Khách thường thì về trang chủ
        }

        // Sai email hoặc mật khẩu
        redirectAttributes.addFlashAttribute("errorMsg", "Email hoặc mật khẩu không chính xác!");
        return "redirect:/login";
    }

    // 3. Xử lý Đăng xuất
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("loggedInUser"); // Xóa session
        return "redirect:/login"; // Đuổi về trang login
    }

    // ==========================================
    // CHỨC NĂNG ĐĂNG KÝ TÀI KHOẢN
    // ==========================================

    // 4. Hiển thị trang Đăng ký
    @GetMapping("/register")
    public String showRegisterPage(HttpSession session) {
        User existing = (User) session.getAttribute("loggedInUser");
        if (existing != null) {
            if ("ADMIN".equals(existing.getRole())) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/";
        }
        return "register"; // Mở file register.html
    }

    // 5. Xử lý khi khách bấm nút "Đăng ký"
    @PostMapping("/register")
    public String processRegister(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra xem 2 ô mật khẩu có gõ giống nhau không
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMsg", "Mật khẩu xác nhận không khớp!");
            return "redirect:/register";
        }

        // Kiểm tra xem Email này đã có ai dùng trong Database chưa
        if (userRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Email này đã được sử dụng! Vui lòng dùng email khác.");
            return "redirect:/register";
        }

        // Nếu mọi thứ ok -> Tạo tài khoản mới
        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password)); // Mã hóa mật khẩu bằng BCrypt trước khi lưu
        newUser.setRole("user"); 

        userRepository.save(newUser);

        // Đăng ký xong thì đá về trang Login kèm thông báo
        redirectAttributes.addFlashAttribute("successMsg", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/login";
    }

    // ==========================================
    // CHỨC NĂNG QUÊN MẬT KHẨU
    // ==========================================

    // 1. Hiển thị trang Quên mật khẩu
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/";
        }
        return "forgot-password"; // Gọi file forgot-password.html
    }

    // 2. Xử lý khi khách bấm nút "Gửi yêu cầu"
    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {

        // Tìm xem email khách nhập có tồn tại trong hệ thống không
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "Email này không tồn tại trong hệ thống!");
            return "redirect:/forgot-password";
        }

        // Tạo một chuỗi mật khẩu ngẫu nhiên dài 8 ký tự (dùng UUID của Java)
        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 8);

        // Lưu mật khẩu tạm thời đã mã hóa vào Database
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        // Thực thi gửi mail chứa mật khẩu tạm thời cho khách
        try {
            emailService.sendForgotPasswordEmail(email, tempPassword);
            redirectAttributes.addFlashAttribute("successMsg", "Mật khẩu mới đã được gửi vào Gmail của bạn. Vui lòng kiểm tra hộp thư!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Có lỗi xảy ra trong quá trình gửi mail: " + e.getMessage());
            return "redirect:/forgot-password";
        }

        // Gửi thành công thì chuyển hướng khách về trang Login để họ đăng nhập bằng pass mới
        return "redirect:/login";
    }
}