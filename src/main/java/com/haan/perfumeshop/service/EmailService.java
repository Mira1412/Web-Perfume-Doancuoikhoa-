package com.haan.perfumeshop.service;

import com.haan.perfumeshop.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private com.haan.perfumeshop.repository.OrderDetailRepository orderDetailRepository;

    // Helper tạo template HTML header
    private String getHtmlHeader(String title) {
        return "<div style=\"font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #c9a96e; border-radius: 12px; overflow: hidden; background-color: #0d0d1a; color: #f0ece4;\">"
             + "  <div style=\"background: linear-gradient(135deg, #c9a96e, #a07840); padding: 30px; text-align: center;\">"
             + "    <h1 style=\"margin: 0; color: #0d0d1a; font-family: 'Playfair Display', Georgia, serif; font-size: 28px; letter-spacing: 2px;\">HALO PERFUME</h1>"
             + "    <p style=\"margin: 5px 0 0; color: #0d0d1a; font-size: 14px; text-transform: uppercase; letter-spacing: 1px;\">" + title + "</p>"
             + "  </div>"
             + "  <div style=\"padding: 30px; line-height: 1.6;\">";
    }

    // Helper tạo template HTML footer
    private String getHtmlFooter() {
        return "  </div>"
             + "  <div style=\"background-color: #141428; padding: 20px; text-align: center; border-top: 1px solid rgba(201, 169, 110, 0.2); font-size: 12px; color: rgba(240, 236, 228, 0.6);\">"
             + "    <p style=\"margin: 0 0 8px;\">Cảm ơn bạn đã đồng hành cùng thương hiệu của chúng tôi!</p>"
             + "    <p style=\"margin: 0;\"><strong>Halo Perfume Shop</strong> | Hotline: 0987.654.321</p>"
             + "  </div>"
             + "</div>";
    }

    // Helper sinh bảng chi tiết sản phẩm HTML từ đơn hàng
    private String getOrderDetailsHtmlTable(Order order) {
        StringBuilder sb = new StringBuilder();
        try {
            java.util.List<com.haan.perfumeshop.model.OrderDetail> details = orderDetailRepository.findByOrder_Id(order.getId());
            if (details != null && !details.isEmpty()) {
                sb.append("<h3 style=\"color: #c9a96e; margin-top: 25px; border-bottom: 1px solid rgba(201, 169, 110, 0.2); padding-bottom: 8px;\">Chi tiết sản phẩm</h3>");
                sb.append("<table style=\"width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 14px;\">");
                sb.append("<thead><tr style=\"border-bottom: 2px solid #c9a96e; text-align: left;\">");
                sb.append("<th style=\"padding: 8px 0; color: #c9a96e;\">Sản phẩm</th>");
                sb.append("<th style=\"padding: 8px 0; color: #c9a96e; text-align: center;\">SL</th>");
                sb.append("<th style=\"padding: 8px 0; color: #c9a96e; text-align: right;\">Giá</th></tr></thead><tbody>");
                
                DecimalFormat df = new DecimalFormat("#,###");
                for (com.haan.perfumeshop.model.OrderDetail detail : details) {
                    String variantText = detail.getVariant() != null ? " (" + detail.getVariant().getDung_tich() + ")" : "";
                    sb.append("<tr style=\"border-bottom: 1px solid rgba(240, 236, 228, 0.1);\">");
                    sb.append("<td style=\"padding: 10px 0;\">").append(detail.getPerfume().getTen_sp()).append(variantText).append("</td>");
                    sb.append("<td style=\"padding: 10px 0; text-align: center;\">").append(detail.getSo_luong_mua()).append("</td>");
                    sb.append("<td style=\"padding: 10px 0; text-align: right; color: #c9a96e;\">").append(df.format(detail.getGia_luc_mua())).append(" đ</td>");
                    sb.append("</tr>");
                }
                sb.append("</tbody></table>");
            }
        } catch (Exception e) {
            log.error("Lỗi khi lấy chi tiết sản phẩm cho email đơn #{}", order.getId(), e);
        }
        return sb.toString();
    }

    // Hàm gửi email xác nhận đơn hàng (chạy bất đồng bộ để không block luồng chính)
    @Async
    public void sendOrderConfirmationEmail(Order order) {
        // Chỉ gửi nếu khách có email
        if (order.getUser() == null || order.getUser().getEmail() == null) {
            log.warn("Bỏ qua gửi email xác nhận — user hoặc email null cho đơn #{}", order.getId());
            return; 
        }

        String toEmail = order.getUser().getEmail();
        String customerName = order.getUser().getFullName() != null ? order.getUser().getFullName() : "Quý khách";
        
        DecimalFormat df = new DecimalFormat("#,###");
        String formattedTotal = df.format(order.getTong_tien());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDate = order.getNgay_dat().format(dtf);

        // Nội dung Email dạng HTML
        String htmlContent = getHtmlHeader("Xác nhận đơn hàng")
                + "<p>Xin chào <strong>" + customerName + "</strong>,</p>"
                + "<p>Cảm ơn bạn đã tin tưởng và mua sắm tại <strong>Halo Shop</strong>!</p>"
                + "<p>Hệ thống đã ghi nhận đơn hàng thành công của bạn với thông tin tổng quan:</p>"
                + "<div style=\"background: #141428; border: 1px solid rgba(201, 169, 110, 0.2); padding: 15px; border-radius: 8px; margin-top: 15px;\">"
                + "  <p style=\"margin: 5px 0;\">📦 <strong>Mã đơn hàng:</strong> <span style=\"color: #c9a96e;\">#" + order.getId() + "</span></p>"
                + "  <p style=\"margin: 5px 0;\">🕒 <strong>Thời gian đặt:</strong> " + formattedDate + "</p>"
                + "  <p style=\"margin: 5px 0;\">💰 <strong>Tổng thanh toán:</strong> <span style=\"color: #c9a96e; font-weight: bold;\">" + formattedTotal + " VNĐ</span></p>"
                + "  <p style=\"margin: 5px 0;\">🚚 <strong>Phương thức:</strong> " + (order.getPhuong_thuc_thanh_toan() != null ? order.getPhuong_thuc_thanh_toan() : "COD") + "</p>"
                + "</div>"
                + getOrderDetailsHtmlTable(order)
                + "<p style=\"margin-top: 25px;\">Chúng tôi sẽ nhanh chóng đóng gói và giao hàng tới bạn trong thời gian sớm nhất.</p>"
                + getHtmlFooter();

        try {
            jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(toEmail);
            helper.setSubject("🌸 Xác nhận đơn hàng #" + order.getId() + " từ Halo Shop");
            helper.setText(htmlContent, true); // true = HTML
            
            mailSender.send(mimeMessage);
            log.info("✅ Gửi email xác nhận đơn #{} tới {} thành công", order.getId(), toEmail);
        } catch (Exception e) {
            log.error("❌ Gửi email xác nhận đơn #{} tới {} thất bại: {}", order.getId(), toEmail, e.getMessage());
        }
    }

    // Hàm gửi mật khẩu tạm thời khi khách quên mật khẩu
    @Async
    public void sendForgotPasswordEmail(String toEmail, String temporaryPassword) {
        String htmlContent = getHtmlHeader("Khôi phục mật khẩu")
                + "<p>Xin chào,</p>"
                + "<p>Hệ thống đã nhận được yêu cầu khôi phục mật khẩu tài khoản liên kết với Email này.</p>"
                + "<div style=\"background: #141428; border: 1px solid rgba(201, 169, 110, 0.2); padding: 20px; border-radius: 8px; text-align: center; margin: 20px 0;\">"
                + "  <p style=\"margin: 0; font-size: 14px;\">Mật khẩu tạm thời của bạn là:</p>"
                + "  <h2 style=\"margin: 10px 0 0; color: #c9a96e; font-family: monospace; font-size: 24px; letter-spacing: 2px;\">" + temporaryPassword + "</h2>"
                + "</div>"
                + "<p style=\"color: #ff6b6b;\">⚠️ <strong>Lưu ý:</strong> Vui lòng đăng nhập lại và đổi mật khẩu mới tại trang <em>Hồ sơ cá nhân</em> ngay lập tức để bảo mật tài khoản.</p>"
                + getHtmlFooter();

        try {
            jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(toEmail);
            helper.setSubject("🌸 Khôi phục mật khẩu tài khoản Halo Shop");
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            log.info("✅ Gửi email khôi phục mật khẩu tới {} thành công", toEmail);
        } catch (Exception e) {
            log.error("❌ Gửi email khôi phục mật khẩu tới {} thất bại: {}", toEmail, e.getMessage());
        }
    }

    // Hàm chuyển đổi trạng thái tiếng Anh sang tiếng Việt để hiển thị email thân thiện hơn
    private String getFriendlyStatus(String status) {
        if (status == null) return "Chờ xử lý";
        switch (status) {
            case "Pending": return "Chờ xử lý ⏳";
            case "Packing": return "Đang đóng gói 📦";
            case "Shipping": return "Đang giao hàng 🚚";
            case "Delivered": return "Đã giao thành công 🎉";
            case "Cancelled": return "Đã hủy ❌";
            default: return status;
        }
    }

    // Hàm gửi email thông báo cập nhật trạng thái đơn hàng (chung cho mọi thao tác của Admin)
    @Async
    public void sendOrderStatusUpdateEmail(Order order) {
        if (order.getUser() == null || order.getUser().getEmail() == null) {
            log.warn("Bỏ qua gửi email cập nhật trạng thái — user hoặc email null cho đơn #{}", order.getId());
            return;
        }

        String toEmail = order.getUser().getEmail();
        String customerName = order.getUser().getFullName() != null ? order.getUser().getFullName() : "Quý khách";
        String statusVi = getFriendlyStatus(order.getTrang_thai());

        DecimalFormat df = new DecimalFormat("#,###");
        String formattedTotal = df.format(order.getTong_tien());

        String htmlContent = getHtmlHeader("Cập nhật đơn hàng")
                + "<p>Xin chào <strong>" + customerName + "</strong>,</p>"
                + "<p>Halo Shop xin thông báo đơn hàng của bạn đã có cập nhật trạng thái mới:</p>"
                + "<div style=\"background: #141428; border: 1px solid rgba(201, 169, 110, 0.2); padding: 15px; border-radius: 8px; margin-top: 15px;\">"
                + "  <p style=\"margin: 5px 0;\">📦 <strong>Mã đơn hàng:</strong> <span style=\"color: #c9a96e;\">#" + order.getId() + "</span></p>"
                + "  <p style=\"margin: 5px 0;\">💰 <strong>Tổng thanh toán:</strong> " + formattedTotal + " VNĐ</p>"
                + "  <p style=\"margin: 5px 0;\">🔄 <strong>Trạng thái mới:</strong> <span style=\"color: #c9a96e; font-weight: bold;\">" + statusVi + "</span></p>"
                + "</div>"
                + getOrderDetailsHtmlTable(order)
                + "<p style=\"margin-top: 25px;\">Bạn có thể truy cập website để kiểm tra chi tiết.</p>"
                + getHtmlFooter();

        try {
            jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(toEmail);
            helper.setSubject("🌸 Cập nhật trạng thái đơn hàng #" + order.getId() + " - Halo Shop");
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            log.info("✅ Gửi email cập nhật trạng thái đơn #{} ({}) tới {} thành công", order.getId(), order.getTrang_thai(), toEmail);
        } catch (Exception e) {
            log.error("❌ Gửi email cập nhật đơn #{} tới {} thất bại: {}", order.getId(), toEmail, e.getMessage());
        }
    }

    // Giữ lại hàm cũ để tránh lỗi compile nếu nơi khác đang dùng (deprecated)
    @Async
    public void sendOrderCancellationEmail(Order order) {
        sendOrderStatusUpdateEmail(order);
    }
}
