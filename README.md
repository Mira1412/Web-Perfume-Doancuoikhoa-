# 🌸 Halo Perfume Shop - Đồ Án Thực Tập Cuối Khóa

Chào mừng đến với mã nguồn của dự án **Halo Perfume Shop**! Đây là một website thương mại điện tử chuyên bán nước hoa cao cấp, được phát triển như một đồ án thực tập cuối khóa. Trang web được thiết kế với giao diện *Dark Luxury* (Kính mờ sang trọng) mang lại trải nghiệm mua sắm hiện đại và tinh tế.

## 💻 Công nghệ sử dụng

Dự án được xây dựng dựa trên kiến trúc MVC với các công nghệ sau:

- **Backend:** Java, Spring Boot, Spring Data JPA, Spring Security (hiện đang tắt CSRF để dễ test).
- **Frontend:** HTML5, CSS3, JavaScript, Thymeleaf (Template Engine), Bootstrap 5.
- **Database:** MySQL.
- **Tools:** Maven, Git, VS Code/IntelliJ IDEA.

## ✨ Các chức năng chính

### Phía Người dùng (Khách hàng)
1. **Xác thực:** 
   - Đăng ký tài khoản với tính năng kiểm tra mật khẩu an toàn.
   - Đăng nhập vào hệ thống.
2. **Cửa hàng & Sản phẩm:** 
   - Xem danh sách toàn bộ nước hoa tại trang chủ.
   - Xem thông tin chi tiết từng sản phẩm (Nhóm hương, Dung tích, Số lượng tồn kho, Giá bán).
3. **Giỏ hàng:** 
   - Thêm sản phẩm vào giỏ hàng (bằng AJAX/Fetch API mượt mà không cần tải lại trang).
   - Tăng/giảm số lượng và tính toán tổng tiền tự động.
4. **Thanh toán & Đơn hàng:** 
   - Chốt đơn hàng trực tiếp từ giỏ. Hệ thống tự động trừ hàng tồn kho theo thời gian thực.
   - Theo dõi lịch sử mua hàng, trạng thái đơn hàng (Pending, Completed...) tại trang "Đơn hàng của tôi".

## 🚀 Hướng dẫn cài đặt và chạy dự án

### Yêu cầu hệ thống:
- Java JDK 17
- Maven 3.6+
- MySQL Server 8.0+

### Các bước khởi chạy:
1. **Tạo Database:**
   Mở MySQL và tạo một database mới:
   ```sql
   CREATE DATABASE perfume_shop;
   ```

2. **Cấu hình kết nối:**
   Mở file `src/main/resources/application.properties` và đảm bảo các thông số kết nối Database đúng với máy của bạn:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/perfume_shop?useSSL=false&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=mat_khau_cua_ban
   ```
   *(Lưu ý: Chế độ `spring.jpa.hibernate.ddl-auto=update` sẽ tự động tạo bảng cho bạn trong lần chạy đầu tiên).*

3. **Chạy ứng dụng:**
   Bạn có thể chạy dự án trực tiếp trên IDE (Eclipse, IntelliJ, VS Code) bằng cách chạy file `PerfumeThesisApplication.java`.
   
   Hoặc chạy qua terminal bằng Maven:
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Trải nghiệm:**
   Mở trình duyệt và truy cập vào địa chỉ: `http://localhost:8081`

---
*Dự án được phát triển bởi Khả Ân (Mira1412) - Đồ án thực tập cuối khóa.*
