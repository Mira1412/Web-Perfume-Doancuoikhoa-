package com.haan.perfumeshop.service;

import com.haan.perfumeshop.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Hàm gửi email xác nhận đơn hàng
    public void sendOrderConfirmationEmail(Order order) {
        // Chỉ gửi nếu khách có email
        if (order.getUser() == null || order.getUser().getEmail() == null) {
            return; 
        }

        String toEmail = order.getUser().getEmail();
        String customerName = order.getUser().getFullName() != null ? order.getUser().getFullName() : "Quý khách";
        
        // Định dạng tiền và thời gian cho đẹp
        DecimalFormat df = new DecimalFormat("#,###");
        String formattedTotal = df.format(order.getTong_tien());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDate = order.getNgay_dat().format(dtf);

        // Nội dung Email
        String subject = "🌸 Xác nhận đơn hàng #" + order.getId() + " từ Halo Shop";
        String body = "Xin chào " + customerName + ",\n\n"
                + "Cảm ơn bạn đã tin tưởng và mua sắm tại Halo Shop!\n"
                + "Hệ thống đã ghi nhận đơn hàng của bạn với các thông tin sau:\n\n"
                + "📦 Mã đơn hàng: #" + order.getId() + "\n"
                + "🕒 Thời gian đặt: " + formattedDate + "\n"
                + "💰 Tổng thanh toán: " + formattedTotal + " VNĐ\n"
                + "🚚 Trạng thái: Đang chờ xử lý\n\n"
                + "Chúng tôi sẽ đóng gói và giao hàng đến bạn trong thời gian sớm nhất.\n"
                + "Bạn có thể xem lại chi tiết đơn hàng tại trang 'Đơn Hàng Của Tôi' trên website.\n\n"
                + "Trân trọng,\n"
                + "Đội ngũ Halo Shop.";

        // Tạo lệnh gửi
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("lucy139200556@gmail.com");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        // Thực thi gửi
        mailSender.send(message);
    }

    // Hàm gửi mật khẩu tạm thời khi khách quên mật khẩu
    public void sendForgotPasswordEmail(String toEmail, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("lucy139200556@gmail.com");
        message.setTo(toEmail);
        message.setSubject("🌸 Khôi phục mật khẩu tài khoản Halo Shop");
        
        String body = "Xin chào,\n\n"
                + "Hệ thống nhận được yêu cầu khôi phục mật khẩu cho tài khoản gắn liền với Email này.\n"
                + "Mật khẩu tạm thời mới của bạn là: " + temporaryPassword + "\n\n"
                + "Vui lòng sử dụng mật khẩu này để đăng nhập lại vào hệ thống. Sau khi đăng nhập thành công, bạn nên vào ngay trang 'Hồ sơ cá nhân' để đổi lại mật khẩu mới nhằm bảo mật tài khoản.\n\n"
                + "Trân trọng,\n"
                + "Đội ngũ Halo Shop.";
                
        message.setText(body);
        mailSender.send(message);
    }
}
