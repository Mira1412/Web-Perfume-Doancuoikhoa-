package com.haan.perfumeshop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * VNPayService — Tích hợp cổng thanh toán VNPay
 * Tài liệu: https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.md
 */
@Service
public class VNPayService {

    private static final Logger log = LoggerFactory.getLogger(VNPayService.class);

    @Value("${vnpay.tmnCode}")
    private String tmnCode;

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    @Value("${vnpay.payUrl}")
    private String payUrl;

    @Value("${vnpay.returnUrl}")
    private String returnUrl;

    // ===================================================
    // 1. TẠO URL THANH TOÁN VNPAY
    // ===================================================

    /**
     * Tạo URL redirect sang cổng VNPay để khách hàng thanh toán.
     *
     * @param orderId   Mã đơn hàng (txnRef)
     * @param amount    Số tiền (VND, chưa nhân 100)
     * @param orderInfo Thông tin đơn hàng
     * @param ipAddress IP của khách hàng
     * @return URL đầy đủ để redirect
     */
    public String createPaymentUrl(String orderId, long amount, String orderInfo, String ipAddress) {
        // VNPay yêu cầu amount * 100
        long vnpAmount = amount * 100;

        // Thời gian tạo và hết hạn (15 phút)
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(cal.getTime());

        cal.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(cal.getTime());

        // Sắp xếp params theo thứ tự alphabet (TreeMap) — bắt buộc theo spec VNPay
        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version",    "2.1.0");
        vnpParams.put("vnp_Command",    "pay");
        vnpParams.put("vnp_TmnCode",    tmnCode);
        vnpParams.put("vnp_Amount",     String.valueOf(vnpAmount));
        vnpParams.put("vnp_CurrCode",   "VND");
        vnpParams.put("vnp_TxnRef",     orderId);
        vnpParams.put("vnp_OrderInfo",  orderInfo);
        vnpParams.put("vnp_OrderType",  "other");
        vnpParams.put("vnp_Locale",     "vn");
        vnpParams.put("vnp_ReturnUrl",  returnUrl);
        vnpParams.put("vnp_IpAddr",     ipAddress);
        vnpParams.put("vnp_CreateDate", createDate);
        vnpParams.put("vnp_ExpireDate", expireDate);

        // Tạo chuỗi hash data và query string
        StringBuilder hashData  = new StringBuilder();
        StringBuilder queryData = new StringBuilder();

        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            String key   = entry.getKey();
            String value = entry.getValue();
            if (value != null && !value.isEmpty()) {
                String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8)
                                                .replace("+", "%20");
                hashData.append(key).append('=').append(encodedValue).append('&');
                queryData.append(key).append('=').append(encodedValue).append('&');
            }
        }

        // Xóa dấu & cuối cùng
        if (hashData.length() > 0) hashData.deleteCharAt(hashData.length() - 1);
        if (queryData.length() > 0) queryData.deleteCharAt(queryData.length() - 1);

        // Tạo chữ ký HMAC-SHA512
        String secureHash = hmacSHA512(hashSecret, hashData.toString());

        String paymentUrl = payUrl + "?" + queryData + "&vnp_SecureHash=" + secureHash;
        log.info("✅ VNPay payment URL tạo thành công cho orderId={}", orderId);
        return paymentUrl;
    }

    // ===================================================
    // 2. XÁC MINH CHỮ KÝ CALLBACK
    // ===================================================

    /**
     * Xác minh chữ ký HMAC-SHA512 từ VNPay gửi về qua return URL.
     * QUAN TRỌNG: Loại bỏ vnp_SecureHash và vnp_SecureHashType trước khi tính hash.
     *
     * @param params Toàn bộ query params từ VNPay callback
     * @return true nếu chữ ký hợp lệ
     */
    public boolean verifySignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isEmpty()) {
            log.warn("⚠️ VNPay callback thiếu vnp_SecureHash");
            return false;
        }

        // Sắp xếp theo alphabet, loại bỏ các field hash
        Map<String, String> signParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if (!"vnp_SecureHash".equals(key) && !"vnp_SecureHashType".equals(key)) {
                signParams.put(key, entry.getValue());
            }
        }

        // Tạo chuỗi hashData từ params đã sắp xếp
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : signParams.entrySet()) {
            String value = entry.getValue();
            if (value != null && !value.isEmpty()) {
                String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8)
                                                .replace("+", "%20");
                hashData.append(entry.getKey()).append('=').append(encodedValue).append('&');
            }
        }
        if (hashData.length() > 0) hashData.deleteCharAt(hashData.length() - 1);

        // So sánh chữ ký tính lại với chữ ký nhận được
        String computedHash = hmacSHA512(hashSecret, hashData.toString());
        boolean valid = computedHash.equalsIgnoreCase(receivedHash);

        if (!valid) {
            log.warn("⚠️ Chữ ký VNPay không khớp. Expected={}, Received={}", computedHash, receivedHash);
        }
        return valid;
    }

    // ===================================================
    // 3. KIỂM TRA KẾT QUẢ THANH TOÁN
    // ===================================================

    /**
     * VNPay trả về mã "00" khi thanh toán thành công.
     *
     * @param responseCode vnp_ResponseCode từ callback
     * @return true nếu thanh toán thành công
     */
    public boolean isPaymentSuccess(String responseCode) {
        return "00".equals(responseCode);
    }

    // ===================================================
    // HELPER — HMAC-SHA512
    // ===================================================

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("❌ Lỗi khi tính HMAC-SHA512: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo chữ ký HMAC-SHA512", e);
        }
    }
}
