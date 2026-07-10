package com.haan.perfumeshop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VNPayServiceTest {

    @InjectMocks
    private VNPayService vnPayService;

    @BeforeEach
    void setUp() {
        // Sử dụng ReflectionTestUtils để tiêm giá trị @Value vào class Service khi chạy unit test độc lập
        ReflectionTestUtils.setField(vnPayService, "tmnCode", "CGXZURGQ");
        ReflectionTestUtils.setField(vnPayService, "hashSecret", "REAZ2ZDFDVEOPVNZJGKCFRNZEUXIMXPN");
        ReflectionTestUtils.setField(vnPayService, "payUrl", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        ReflectionTestUtils.setField(vnPayService, "returnUrl", "http://localhost:8081/payment/vnpay-return");
    }

    @Test
    void testCreatePaymentUrl_Success() {
        String url = vnPayService.createPaymentUrl("ORDER123", 500000L, "Thanh toan nuoc hoa", "127.0.0.1");

        assertNotNull(url);
        assertTrue(url.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"));
        assertTrue(url.contains("vnp_Amount=50000000")); // Số tiền phải nhân 100 theo spec VNPay
        assertTrue(url.contains("vnp_TxnRef=ORDER123"));
        assertTrue(url.contains("vnp_SecureHash="));
    }

    @Test
    void testVerifySignature_Success() {
        // Giả lập callback params từ VNPay đã có signature hợp lệ
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TmnCode", "CGXZURGQ");
        params.put("vnp_Amount", "50000000");
        params.put("vnp_TxnRef", "ORDER123");
        params.put("vnp_ResponseCode", "00");
        
        // Tính chữ ký chính xác của mock params trên
        // (Sử dụng hashSecret giả lập ở setUp: REAZ2ZDFDVEOPVNZJGKCFRNZEUXIMXPN)
        // Chạy thử hàm verify với chữ ký tính lại
        String url = vnPayService.createPaymentUrl("ORDER123", 500000L, "Test", "127.0.0.1");
        
        // Trích xuất SecureHash từ URL được tạo ra để verify ngược lại
        String secureHash = "";
        String[] parts = url.split("vnp_SecureHash=");
        if (parts.length > 1) {
            secureHash = parts[1];
        }

        // Tạo map params callback tương ứng
        Map<String, String> callbackParams = new HashMap<>();
        callbackParams.put("vnp_TmnCode", "CGXZURGQ");
        callbackParams.put("vnp_Amount", "50000000");
        callbackParams.put("vnp_TxnRef", "ORDER123");
        callbackParams.put("vnp_OrderInfo", "Test");
        callbackParams.put("vnp_SecureHash", secureHash);
        
        // Cần truyền tất cả các tham số được gen trong URL
        // Để khớp chính xác, ta bóc tách toàn bộ query string của paymentUrl để làm params test
        Map<String, String> fullParams = extractParamsFromUrl(url);
        boolean isValid = vnPayService.verifySignature(fullParams);
        assertTrue(isValid, "Chữ ký VNPay phải hợp lệ");
    }

    @Test
    void testIsPaymentSuccess() {
        assertTrue(vnPayService.isPaymentSuccess("00"));
        assertFalse(vnPayService.isPaymentSuccess("07"));
        assertFalse(vnPayService.isPaymentSuccess("99"));
    }

    private Map<String, String> extractParamsFromUrl(String url) {
        Map<String, String> params = new HashMap<>();
        String[] urlParts = url.split("\\?");
        if (urlParts.length > 1) {
            String[] queryParams = urlParts[1].split("&");
            for (String param : queryParams) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    params.put(pair[0], pair[1]);
                } else if (pair.length > 0) {
                    params.put(pair[0], "");
                }
            }
        }
        // decode values để verify đúng
        Map<String, String> decodedParams = new HashMap<>();
        params.forEach((k, v) -> {
            try {
                decodedParams.put(k, java.net.URLDecoder.decode(v, "UTF-8"));
            } catch (Exception e) {
                decodedParams.put(k, v);
            }
        });
        return decodedParams;
    }
}
