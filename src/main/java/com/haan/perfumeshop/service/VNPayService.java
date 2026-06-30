package com.haan.perfumeshop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * VNPayService — Dịch vụ tích hợp cổng thanh toán VNPay
 * Hỗ trợ: Tạo URL thanh toán + Xác minh chữ ký callback
 */
@Service
public class VNPayService {

    @Value("${vnpay.tmnCode}")
    private String tmnCode;

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    @Value("${vnpay.payUrl}")
    private String payUrl;

    @Value("${vnpay.returnUrl}")
    private String returnUrl;

    /**
     * Tạo URL chuyển hướng sang trang thanh toán VNPay
     *
     * @param orderId   ID đơn hàng tạm (dùng làm mã giao dịch)
     * @param amount    Số tiền thanh toán (VND)
     * @param orderInfo Thông tin đơn hàng (hiển thị trên trang VNPay)
     * @param ipAddress IP của khách hàng
     * @return URL redirect sang VNPay
     */
    public String createPaymentUrl(String orderId, long amount, String orderInfo, String ipAddress) {
        try {
            // 1. Thời gian (timezone GMT+7 — Việt Nam)
            TimeZone vnTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            formatter.setTimeZone(vnTimeZone);

            String vnp_CreateDate = formatter.format(new Date());

            Calendar cal = Calendar.getInstance(vnTimeZone);
            cal.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cal.getTime());

            // 2. Mã giao dịch — chỉ dùng số và chữ, không ký tự đặc biệt
            String vnp_TxnRef = String.valueOf(System.currentTimeMillis());

            // 3. Xây dựng Map tham số (TreeMap để tự sắp xếp alphabet — BẮT BUỘC)
            Map<String, String> vnp_Params = new TreeMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", tmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu nhân 100
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", returnUrl);
            vnp_Params.put("vnp_IpAddr", ipAddress != null ? ipAddress : "127.0.0.1");
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            // 4. Xây dựng hashData và query (theo chính xác VNPay Java SDK chính thức)
            // hashData : key=URLEncode(value, US_ASCII)   ← dùng để tính HMAC
            // query    : URLEncode(key)=URLEncode(value)  ← ghép vào URL cuối
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<Map.Entry<String, String>> itr = vnp_Params.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, String> entry = itr.next();
                String key = entry.getKey();
                String value = entry.getValue();
                if (value == null || value.isEmpty()) continue;

                String encodedValue = URLEncoder.encode(value, StandardCharsets.US_ASCII);

                // Hash: key NOT encoded, value encoded US_ASCII
                hashData.append(key).append('=').append(encodedValue);
                // Query: both key and value encoded
                query.append(URLEncoder.encode(key, StandardCharsets.US_ASCII))
                     .append('=').append(encodedValue);

                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }

            // 5. Ký HMAC SHA512 với chuỗi raw
            String secureHash = hmacSHA512(hashSecret, hashData.toString());
            query.append("&vnp_SecureHash=").append(secureHash);

            return payUrl + "?" + query;

        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo URL thanh toán VNPay: " + e.getMessage(), e);
        }
    }

    /**
     * Xác minh chữ ký callback từ VNPay (tránh giả mạo)
     *
     * @param params Tất cả tham số VNPay trả về (trong query string)
     * @return true nếu chữ ký hợp lệ
     */
    public boolean verifySignature(Map<String, String> params) {
        try {
            String vnp_SecureHash = params.get("vnp_SecureHash");
            if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
                return false;
            }

            // Loại bỏ vnp_SecureHash và vnp_SecureHashType khỏi tham số trước khi ký lại
            Map<String, String> checkParams = new TreeMap<>(params);
            checkParams.remove("vnp_SecureHash");
            checkParams.remove("vnp_SecureHashType");

            // Xây dựng lại chuỗi hash đúng theo VNPay Java SDK chính thức
            StringBuilder hashData = new StringBuilder();
            Iterator<Map.Entry<String, String>> itr = checkParams.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, String> entry = itr.next();
                String value = entry.getValue();
                if (value == null || value.isEmpty()) continue;
                hashData.append(entry.getKey()).append('=')
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (itr.hasNext()) hashData.append('&');
            }

            String computedHash = hmacSHA512(hashSecret, hashData.toString());
            return computedHash.equalsIgnoreCase(vnp_SecureHash);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Kiểm tra giao dịch thành công hay không
     *
     * @param responseCode vnp_ResponseCode từ VNPay (00 = thành công)
     * @return true nếu thanh toán thành công
     */
    public boolean isPaymentSuccess(String responseCode) {
        return "00".equals(responseCode);
    }

    // ===================================
    // HMAC SHA512 Helper
    // ===================================
    private String hmacSHA512(String key, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac.init(secretKey);
        byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
