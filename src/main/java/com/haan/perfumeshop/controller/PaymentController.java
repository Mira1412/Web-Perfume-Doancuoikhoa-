package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Cart;
import com.haan.perfumeshop.model.Order;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.service.CartService;
import com.haan.perfumeshop.service.OrderService;
import com.haan.perfumeshop.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.http.ResponseEntity;
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

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final CartService cartService;
    private final OrderService orderService;
    private final VNPayService vnPayService;

    public PaymentController(CartService cartService, OrderService orderService, VNPayService vnPayService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.vnPayService = vnPayService;
    }

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
            log.error("❌ Lỗi khi đặt hàng COD cho user #{}: {}", currentUser.getId_user(), e.getMessage(), e);
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
            String orderInfo = "ThanhToanDonHangHaloPerfume";

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

        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String amountStr    = params.get("vnp_Amount");

        // Xác minh chữ ký từ VNPay (bọc try-catch đề phòng params lạ)
        boolean validSignature;
        try {
            validSignature = vnPayService.verifySignature(params);
        } catch (Exception e) {
            log.error("❌ Lỗi khi xác minh chữ ký VNPay: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("paymentSuccess", false);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xác minh chữ ký thanh toán.");
            return "redirect:/payment/result";
        }

        if (!validSignature) {
            log.warn("⚠️ Chữ ký VNPay không hợp lệ! TransactionNo={}, ResponseCode={}", transactionNo, responseCode);
            redirectAttributes.addFlashAttribute("paymentSuccess", false);
            redirectAttributes.addFlashAttribute("errorMessage", "Chữ ký không hợp lệ! Giao dịch có thể bị giả mạo.");
            return "redirect:/payment/result";
        }

        if (vnPayService.isPaymentSuccess(responseCode)) {
            // Thanh toán VNPay thành công → Tạo đơn hàng
            if (currentUser != null) {
                try {
                    Order order = orderService.checkoutOrder(currentUser, "VNPay", transactionNo);

                    // Xóa session pendingOrder
                    session.removeAttribute("pendingVNPayOrderId");

                    log.info("✅ Đơn hàng VNPay #{} tạo thành công cho user #{}", order.getId(), currentUser.getId_user());
                    redirectAttributes.addFlashAttribute("paymentSuccess", true);
                    redirectAttributes.addFlashAttribute("paymentMethod", "VNPay");
                    redirectAttributes.addFlashAttribute("orderId", order.getId());
                    redirectAttributes.addFlashAttribute("orderTotal", order.getTong_tien());
                    redirectAttributes.addFlashAttribute("transactionNo", transactionNo);
                } catch (Exception e) {
                    log.error("❌ Thanh toán VNPay thành công nhưng lỗi tạo đơn hàng! TransactionNo={}: {}",
                            transactionNo, e.getMessage(), e);
                    redirectAttributes.addFlashAttribute("paymentSuccess", false);
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Thanh toán thành công nhưng lỗi khi tạo đơn hàng: " + e.getMessage());
                }
            } else {
                // ⚠️ EDGE CASE: Phiên đăng nhập đã hết hạn khi VNPay callback.
                // Tiền có thể đã bị trừ nhưng đơn hàng KHÔNG được tạo.
                // Cần xử lý đối soát thủ công hoặc tự động qua VNPay IPN.
                log.warn("⚠️ VNPay callback nhưng session hết hạn! TransactionNo={}, Amount={}",
                         transactionNo, amountStr);
                redirectAttributes.addFlashAttribute("paymentSuccess", false);
                redirectAttributes.addFlashAttribute("errorMessage", "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại.");
            }
        } else {
            // Thanh toán thất bại hoặc bị hủy
            String errorMsg = getVNPayErrorMessage(responseCode);
            log.info("ℹ️ VNPay thanh toán thất bại. TransactionNo={}, ResponseCode={}", transactionNo, responseCode);
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

    private static final Map<String, String> VNPAY_ERROR_MESSAGES = Map.ofEntries(
            Map.entry("07", "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên hệ VNPay)."),
            Map.entry("09", "Thẻ/Tài khoản chưa đăng ký Internet Banking."),
            Map.entry("10", "Xác thực thẻ/tài khoản sai quá 3 lần."),
            Map.entry("11", "Đã hết hạn chờ thanh toán. Vui lòng thực hiện lại giao dịch."),
            Map.entry("12", "Thẻ/Tài khoản bị khóa."),
            Map.entry("13", "Sai mật khẩu OTP. Vui lòng thực hiện lại giao dịch."),
            Map.entry("24", "Khách hàng hủy giao dịch."),
            Map.entry("51", "Tài khoản không đủ số dư để thực hiện giao dịch."),
            Map.entry("65", "Tài khoản đã vượt quá hạn mức giao dịch trong ngày."),
            Map.entry("75", "Ngân hàng thanh toán đang bảo trì."),
            Map.entry("79", "Sai mật khẩu thanh toán quá số lần quy định.")
    );

    private String getVNPayErrorMessage(String responseCode) {
        if (responseCode == null) return "Giao dịch thất bại!";
        return VNPAY_ERROR_MESSAGES.getOrDefault(responseCode, "Giao dịch thất bại! Mã lỗi: " + responseCode);
    }
}
