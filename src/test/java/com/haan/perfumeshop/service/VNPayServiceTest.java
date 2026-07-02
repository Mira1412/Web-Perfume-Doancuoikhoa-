package com.haan.perfumeshop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VNPayServiceTest {

    private VNPayService vnPayService;

    @BeforeEach
    void setUp() {
        vnPayService = new VNPayService();
        // Inject cấu hình VNPay giả lập để chạy test độc lập
        ReflectionTestUtils.setField(vnPayService, "tmnCode", "N77AE6DX");
        ReflectionTestUtils.setField(vnPayService, "hashSecret", "DF7DRKZA51AL9BKT3GWM8XLQB4ZIU89R");
        ReflectionTestUtils.setField(vnPayService, "payUrl", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        ReflectionTestUtils.setField(vnPayService, "returnUrl", "http://localhost:8081/payment/vnpay-return");
    }

    @Test
    void testCreatePaymentUrl() {
        String orderId = "HALO_TEST_12345";
        long amount = 500000; // 500,000 VND
        String orderInfo = "Thanh toan don hang test";
        String ipAddress = "127.0.0.1";

        String url = vnPayService.createPaymentUrl(orderId, amount, orderInfo, ipAddress);

        assertNotNull(url);
        assertTrue(url.contains("vnp_TmnCode=N77AE6DX"));
        assertTrue(url.contains("vnp_Amount=50000000")); // VNPay yêu cầu nhân 100
        assertTrue(url.contains("vnp_SecureHash="));
    }

    @Test
    void testIsPaymentSuccess() {
        assertTrue(vnPayService.isPaymentSuccess("00"));
        assertFalse(vnPayService.isPaymentSuccess("09"));
        assertFalse(vnPayService.isPaymentSuccess("24"));
    }

    // ===================================================
    // BỔ SUNG: Test xác minh chữ ký (verifySignature)
    // ===================================================

    @Test
    void testVerifySignature_ValidSignature() {
        // Tạo URL thanh toán để lấy secureHash chính xác
        String url = vnPayService.createPaymentUrl("TEST_ORDER_1", 100000, "Test order", "127.0.0.1");

        // Trích xuất các tham số từ URL
        Map<String, String> params = new HashMap<>();
        String queryString = url.substring(url.indexOf("?") + 1);
        for (String param : queryString.split("&")) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                try {
                    params.put(keyValue[0], java.net.URLDecoder.decode(keyValue[1], "UTF-8"));
                } catch (Exception e) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }

        // Chữ ký phải hợp lệ vì được tạo bởi cùng hashSecret
        assertTrue(vnPayService.verifySignature(params));
    }

    @Test
    void testVerifySignature_InvalidSignature() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "10000000");
        params.put("vnp_TxnRef", "TEST_ORDER_FAKE");
        params.put("vnp_SecureHash", "chu_ky_gia_mao_12345abcdef");

        // Chữ ký giả mạo → phải trả về false
        assertFalse(vnPayService.verifySignature(params));
    }

    @Test
    void testVerifySignature_NullSignature() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "10000000");
        params.put("vnp_TxnRef", "TEST_ORDER");
        // Không có vnp_SecureHash

        assertFalse(vnPayService.verifySignature(params));
    }

    @Test
    void testCreatePaymentUrl_ContainsRequiredParams() {
        String url = vnPayService.createPaymentUrl("HALO_99", 250000, "Don hang 99", "192.168.1.1");

        assertNotNull(url);
        assertTrue(url.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?"));
        assertTrue(url.contains("vnp_Version="));
        assertTrue(url.contains("vnp_Command=pay"));
        assertTrue(url.contains("vnp_TmnCode=N77AE6DX"));
        assertTrue(url.contains("vnp_CurrCode=VND"));
        assertTrue(url.contains("vnp_OrderInfo="));
        assertTrue(url.contains("vnp_ReturnUrl="));
        assertTrue(url.contains("vnp_CreateDate="));
        assertTrue(url.contains("vnp_ExpireDate="));
        assertTrue(url.contains("vnp_IpAddr="));
    }

    @Test
    void testCreatePaymentUrl_HandlesIPv6Localhost() {
        // IPv6 localhost "0:0:0:0:0:0:0:1" phải được chuyển thành "127.0.0.1"
        String url = vnPayService.createPaymentUrl("HALO_IPV6", 100000, "IPv6 test", "0:0:0:0:0:0:0:1");

        assertNotNull(url);
        assertTrue(url.contains("vnp_IpAddr=127.0.0.1"));
        assertFalse(url.contains("0%3A0%3A0")); // Không chứa IPv6 dạng encoded
    }

    // ===================================================
    // BỔ SUNG: Test validation input
    // ===================================================

    @Test
    void testCreatePaymentUrl_NullOrderId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            vnPayService.createPaymentUrl(null, 100000, "Test", "127.0.0.1");
        });
    }

    @Test
    void testCreatePaymentUrl_ZeroAmount_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            vnPayService.createPaymentUrl("ORDER_1", 0, "Test", "127.0.0.1");
        });
    }

    @Test
    void testCreatePaymentUrl_NegativeAmount_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            vnPayService.createPaymentUrl("ORDER_1", -50000, "Test", "127.0.0.1");
        });
    }
}
