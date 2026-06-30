package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Cart;
import com.haan.perfumeshop.model.Order;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.service.CartService;
import com.haan.perfumeshop.service.OrderService;
import com.haan.perfumeshop.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PaymentController — Quản lý toàn bộ luồng thanh toán
 * Routes:
 *   GET  /checkout              → Trang xác nhận đơn hàng + chọn phương thức
 *   POST /payment/create-vnpay  → Tạo URL VNPay và redirect
 *   POST /payment/cod           → Đặt hàng COD (thanh toán khi nhận hàng)
 *   GET  /payment/vnpay-return  → Nhận callback từ VNPay
 */
@Controller
public class PaymentController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private VNPayService vnPayService;

    // ===================================================
    // 1. TRANG XÁC NHẬN ĐƠN HÀNG — /checkout
    // ===================================================
    @GetMapping("/checkout")
    public String showCheckout(HttpSession session, Model model) {
        // Kiểm tra đăng nhập
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Lấy giỏ hàng
        List<Cart> cartItems = cartService.getCartByUserId(currentUser.getId_user());
        if (cartItems.isEmpty()) {
            return "redirect:/cart"; // Giỏ trống → quay về giỏ
        }

        // Tính tổng tiền
        double totalPrice = cartItems.stream()
                .mapToDouble(item -> item.getPriceNumeric() * item.getSo_luong())
                .sum();

        model.addAttribute("user", currentUser);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);

        return "checkout";
    }

    // ===================================================
    // 2. ĐẶT HÀNG COD — POST /payment/cod
    // ===================================================
    @PostMapping("/payment/cod")
    public String processCOD(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            Order order = orderService.checkoutOrder(currentUser, "COD", null);
            redirectAttributes.addFlashAttribute("paymentSuccess", true);
            redirectAttributes.addFlashAttribute("paymentMethod", "COD");
            redirectAttributes.addFlashAttribute("orderId", order.getId());
            redirectAttributes.addFlashAttribute("orderTotal", order.getTong_tien());
            return "redirect:/payment/result";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("paymentSuccess", false);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/payment/result";
        }
    }

    // ===================================================
    // 3. TẠO LINK VNPAY — POST /payment/create-vnpay
    // ===================================================
    @PostMapping("/payment/create-vnpay")
    public String createVNPayPayment(
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            // Lấy giỏ hàng và tính tổng tiền
            List<Cart> cartItems = cartService.getCartByUserId(currentUser.getId_user());
            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("paymentSuccess", false);
                redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống!");
                return "redirect:/payment/result";
            }

            double totalPrice = cartItems.stream()
                    .mapToDouble(item -> item.getPriceNumeric() * item.getSo_luong())
                    .sum();
            long amount = (long) totalPrice;

            // Thông tin đơn hàng cho VNPay
            String orderId = "HALO_" + currentUser.getId_user() + "_" + System.currentTimeMillis() % 10000;
            String orderInfo = "Thanh toan don hang Halo Perfume - User " + currentUser.getId_user();

            // Lấy IP của client
            String ipAddress = getClientIpAddress(request);

            // Lưu orderId vào session để dùng khi nhận callback
            session.setAttribute("pendingVNPayOrderId", orderId);

            // Tạo URL và redirect sang VNPay
            String paymentUrl = vnPayService.createPaymentUrl(orderId, amount, orderInfo, ipAddress);
            return "redirect:" + paymentUrl;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("paymentSuccess", false);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể tạo thanh toán VNPay: " + e.getMessage());
            return "redirect:/payment/result";
        }
    }

    // ===================================================
    // 4. NHẬN CALLBACK TỪ VNPAY — GET /payment/vnpay-return
    // ===================================================
    @GetMapping("/payment/vnpay-return")
    public String vnpayReturn(
            @RequestParam Map<String, String> params,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("loggedInUser");

        // Xác minh chữ ký từ VNPay
        boolean validSignature = vnPayService.verifySignature(params);
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String amountStr = params.get("vnp_Amount");

        if (!validSignature) {
            redirectAttributes.addFlashAttribute("paymentSuccess", false);
            redirectAttributes.addFlashAttribute("errorMessage", "Chữ ký không hợp lệ! Giao dịch có thể bị giả mạo.");
            return "redirect:/payment/result";
        }

        if (vnPayService.isPaymentSuccess(responseCode)) {
            // Thanh toán VNPay thành công → Tạo đơn hàng
            if (currentUser != null) {
                try {
                    double amount = amountStr != null ? Double.parseDouble(amountStr) / 100 : 0;
                    Order order = orderService.checkoutOrder(currentUser, "VNPay", transactionNo);

                    // Xóa session pendingOrder
                    session.removeAttribute("pendingVNPayOrderId");

                    redirectAttributes.addFlashAttribute("paymentSuccess", true);
                    redirectAttributes.addFlashAttribute("paymentMethod", "VNPay");
                    redirectAttributes.addFlashAttribute("orderId", order.getId());
                    redirectAttributes.addFlashAttribute("orderTotal", order.getTong_tien());
                    redirectAttributes.addFlashAttribute("transactionNo", transactionNo);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("paymentSuccess", false);
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Thanh toán thành công nhưng lỗi khi tạo đơn hàng: " + e.getMessage());
                }
            } else {
                redirectAttributes.addFlashAttribute("paymentSuccess", false);
                redirectAttributes.addFlashAttribute("errorMessage", "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại.");
            }
        } else {
            // Thanh toán thất bại hoặc bị hủy
            String errorMsg = getVNPayErrorMessage(responseCode);
            redirectAttributes.addFlashAttribute("paymentSuccess", false);
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            redirectAttributes.addFlashAttribute("responseCode", responseCode);
        }

        return "redirect:/payment/result";
    }

    // ===================================================
    // 5. TRANG KẾT QUẢ THANH TOÁN — GET /payment/result
    // ===================================================
    @GetMapping("/payment/result")
    public String showPaymentResult(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        model.addAttribute("user", currentUser);
        return "payment-result";
    }

    // ===================================================
    // HELPER METHODS
    // ===================================================
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED", "HTTP_CLIENT_IP", "HTTP_VIA", "REMOTE_ADDR"
        };
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private String getVNPayErrorMessage(String responseCode) {
        if (responseCode == null) return "Giao dịch thất bại!";
        Map<String, String> messages = new HashMap<>();
        messages.put("07", "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên hệ VNPay).");
        messages.put("09", "Thẻ/Tài khoản chưa đăng ký Internet Banking.");
        messages.put("10", "Xác thực thẻ/tài khoản sai quá 3 lần.");
        messages.put("11", "Đã hết hạn chờ thanh toán. Vui lòng thực hiện lại giao dịch.");
        messages.put("12", "Thẻ/Tài khoản bị khóa.");
        messages.put("13", "Sai mật khẩu OTP. Vui lòng thực hiện lại giao dịch.");
        messages.put("24", "Khách hàng hủy giao dịch.");
        messages.put("51", "Tài khoản không đủ số dư để thực hiện giao dịch.");
        messages.put("65", "Tài khoản đã vượt quá hạn mức giao dịch trong ngày.");
        messages.put("75", "Ngân hàng thanh toán đang bảo trì.");
        messages.put("79", "Sai mật khẩu thanh toán quá số lần quy định.");
        return messages.getOrDefault(responseCode, "Giao dịch thất bại! Mã lỗi: " + responseCode);
    }
}
