package com.haan.perfumeshop.service;

import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // =============================================
    // LOGIC ĐĂNG KÝ TÀI KHOẢN
    // =============================================
    public User registerUser(String fullName, String email, String phone,
                             String password, String confirmPassword) throws Exception {

        // 1. Kiểm tra các trường bắt buộc không được để trống
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new Exception("Vui lòng nhập họ và tên của bạn!");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("Vui lòng nhập địa chỉ email!");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new Exception("Vui lòng nhập mật khẩu!");
        }

        // 2. Kiểm tra độ dài mật khẩu tối thiểu 6 ký tự
        if (password.length() < 6) {
            throw new Exception("Mật khẩu phải có ít nhất 6 ký tự!");
        }

        // 3. Kiểm tra mật khẩu và xác nhận mật khẩu có khớp không
        if (!password.equals(confirmPassword)) {
            throw new Exception("Mật khẩu xác nhận không khớp, vui lòng kiểm tra lại!");
        }

        // 4. Kiểm tra email đã tồn tại trong hệ thống chưa
        User existingUser = userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
        if (existingUser != null) {
            throw new Exception("Email \"" + email + "\" đã được đăng ký. Vui lòng dùng email khác!");
        }

        // 5. Tạo user mới và lưu vào database
        User newUser = new User();
        newUser.setFullName(fullName.trim());
        newUser.setEmail(email.trim().toLowerCase());
        newUser.setPhone(phone != null ? phone.trim() : "");
        newUser.setPassword(password);       // Lưu ý: thực tế nên mã hóa bằng BCrypt
        newUser.setRole("customer");         // Mặc định quyền là khách hàng

        return userRepository.save(newUser);
    }

    // =============================================
    // LOGIC ĐĂNG NHẬP
    // =============================================
    public User loginUser(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("Vui lòng nhập địa chỉ email!");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new Exception("Vui lòng nhập mật khẩu!");
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);

        // Nếu không tìm thấy user hoặc sai mật khẩu
        if (user == null || !user.getPassword().equals(password)) {
            throw new Exception("Sai địa chỉ email hoặc mật khẩu. Vui lòng kiểm tra lại!");
        }

        return user; // Trả về thông tin user nếu đăng nhập thành công
    }
}