package com.haan.perfumeshop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * VNPayService — Tích hợp cổng thanh toán VNPay (Đã Fix lỗi Invalid Signature)
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
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String createDate = formatter.format(cal.getTime());

        cal.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(cal.getTime());

        // Xử lý orderInfo: bỏ dấu tiếng Việt (VNPay không hỗ trợ Unicode có dấu)
        String safeOrderInfo = removeVietnameseDiacritics(orderInfo);

        // Xử lý IP: chuyển IPv6 loopback sang IPv4
        if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }

        // Đưa dữ liệu vào Map
        // QUAN TRỌNG: TUYỆT ĐỐI KHÔNG đưa vnp_SecureHash hoặc vnp_SecureHashType vào
        // đây!
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", tmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnpAmount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", orderId);
        vnp_Params.put("vnp_OrderInfo", safeOrderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        vnp_Params.put("vnp_IpAddr", ipAddress);
        vnp_Params.put("vnp_CreateDate", createDate);
        vnp_Params.put("vnp_ExpireDate", expireDate);

        // 1. Lấy danh sách Key và sắp xếp theo Alphabet
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        // 2. Vòng lặp nối chuỗi chuẩn VNPAY (Đã Fix chuẩn mã hoá URL và dấu &)
        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {

                if (hashData.length() > 0) {
                    hashData.append('&');
                    query.append('&');
                }

                // Bắt buộc thay thế '+' thành '%20' theo chuẩn VNPay
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII).replace("+", "%20");
                String encodedName = URLEncoder.encode(fieldName, StandardCharsets.US_ASCII).replace("+", "%20");

                // Build hash data
                hashData.append(fieldName).append('=').append(encodedValue);
                // Build query
                query.append(encodedName).append('=').append(encodedValue);
            }
        }

        // 3. Tạo chữ ký
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(hashSecret, hashData.toString());

        // 4. Cộng chữ ký vào URL cuối cùng
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = payUrl + "?" + queryUrl;

        // In ra console để kiểm tra (có thể xóa khi deploy)
        System.out.println("HASH DATA: " + hashData.toString());
        System.out.println("PAYMENT URL: " + paymentUrl);

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

        // Lọc CHỈ lấy các param của VNPay, bỏ qua param lạ và SecureHash
        Map<String, String> signParams = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if (key != null && key.startsWith("vnp_") && !"vnp_SecureHash".equals(key)
                    && !"vnp_SecureHashType".equals(key)) {
                signParams.put(key, entry.getValue());
            }
        }

        // Sắp xếp theo alphabet
        List<String> fieldNames = new ArrayList<>(signParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();

        // Vòng lặp nối chuỗi chuẩn VNPay khi verify
        for (String fieldName : fieldNames) {
            String fieldValue = signParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                // Ép chuẩn '%20' khi nhận dữ liệu về
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII).replace("+", "%20");
                hashData.append(fieldName).append('=').append(encodedValue);
            }
        }

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

    public String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    // ===================================================
    // HELPER — Bỏ dấu tiếng Việt
    // ===================================================

    /**
     * Chuyển chuỗi tiếng Việt có dấu thành không dấu.
     * Ví dụ: "Thanh toán đơn hàng" → "Thanh toan don hang"
     * VNPay yêu cầu vnp_OrderInfo không chứa ký tự Unicode có dấu.
     */
    private String removeVietnameseDiacritics(String str) {
        if (str == null)
            return "";
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        // Loại bỏ các combining diacritical marks
        String noDiacritics = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        // Xử lý riêng chữ đ/Đ (Normalizer không xử lý)
        noDiacritics = noDiacritics.replace('đ', 'd').replace('Đ', 'D');
        return noDiacritics;
    }
}