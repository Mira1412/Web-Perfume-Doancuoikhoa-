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
}
