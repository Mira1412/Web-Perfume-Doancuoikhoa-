package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Cart;
import com.haan.perfumeshop.model.Order;
import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.model.PerfumeVariant;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.service.CartService;
import com.haan.perfumeshop.service.OrderService;
import com.haan.perfumeshop.service.VNPayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartService cartService;

    @Mock
    private OrderService orderService;

    @Mock
    private VNPayService vnPayService;

    @InjectMocks
    private PaymentController paymentController;

    private User mockUser;
    private MockHttpSession session;
    private Cart mockCartItem;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        // Fix cho Thymeleaf: sử dụng InternalResourceViewResolver tránh lỗi circular view path
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                .setViewResolvers(viewResolver)
                .build();

        // User giả lập
        mockUser = new User();
        mockUser.setId_user(1L);
        mockUser.setFullName("Test User");
        mockUser.setEmail("test@gmail.com");

        // Session giả lập
        session = new MockHttpSession();

        // Cart item giả lập
        PerfumeVariant variant = new PerfumeVariant();
        variant.setId_bien_the(10L);
        variant.setGia_ban("500000");
        variant.setDung_tich("50ml");

        Perfume perfume = new Perfume();
        perfume.setId_nuoc_hoa(100L);
        perfume.setTen_sp("Test Perfume");
        List<PerfumeVariant> variants = new ArrayList<>();
        variants.add(variant);
        perfume.setVariants(variants);

        mockCartItem = new Cart();
        mockCartItem.setId(1L);
        mockCartItem.setUser(mockUser);
        mockCartItem.setPerfume(perfume);
        mockCartItem.setVariant(variant);
        mockCartItem.setSo_luong(2);

        // Order giả lập
        mockOrder = new Order();
        mockOrder.setId(999L);
        mockOrder.setUser(mockUser);
        mockOrder.setTong_tien(1000000.0);
        mockOrder.setTrang_thai("Pending");
    }

    // ===================================================
    // 1. GET /checkout — Đã đăng nhập, có giỏ hàng
    // ===================================================
    @Test
    void testShowCheckout_LoggedIn() throws Exception {
        session.setAttribute("loggedInUser", mockUser);
        when(cartService.getCartByUserId(1L)).thenReturn(List.of(mockCartItem));

        mockMvc.perform(get("/checkout").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("cartItems"))
                .andExpect(model().attributeExists("totalPrice"));
    }

    // ===================================================
    // 2. GET /checkout — Chưa đăng nhập → redirect login
    // ===================================================
    @Test
    void testShowCheckout_NotLoggedIn_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/checkout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ===================================================
    // 3. POST /payment/cod — Thành công
    // ===================================================
    @Test
    void testProcessCOD_Success() throws Exception {
        session.setAttribute("loggedInUser", mockUser);
        when(orderService.checkoutOrder(any(User.class), eq("COD"), isNull())).thenReturn(mockOrder);

        mockMvc.perform(post("/payment/cod").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/result"))
                .andExpect(flash().attribute("paymentSuccess", true))
                .andExpect(flash().attribute("paymentMethod", "COD"))
                .andExpect(flash().attribute("orderId", 999L));
    }

    // ===================================================
    // 4. POST /payment/cod — Chưa đăng nhập
    // ===================================================
    @Test
    void testProcessCOD_NotLoggedIn() throws Exception {
        mockMvc.perform(post("/payment/cod").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ===================================================
    // 5. GET /payment/vnpay-return — Thanh toán VNPay thành công
    // ===================================================
    @Test
    void testVnpayReturn_SuccessPayment() throws Exception {
        session.setAttribute("loggedInUser", mockUser);
        when(vnPayService.verifySignature(anyMap())).thenReturn(true);
        when(vnPayService.isPaymentSuccess("00")).thenReturn(true);
        when(orderService.checkoutOrder(any(User.class), eq("VNPay"), eq("TXN999"))).thenReturn(mockOrder);

        mockMvc.perform(get("/payment/vnpay-return")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_TransactionNo", "TXN999")
                        .param("vnp_Amount", "100000000")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/result"))
                .andExpect(flash().attribute("paymentSuccess", true))
                .andExpect(flash().attribute("paymentMethod", "VNPay"))
                .andExpect(flash().attribute("transactionNo", "TXN999"));
    }

    // ===================================================
    // 6. GET /payment/vnpay-return — Chữ ký không hợp lệ
    // ===================================================
    @Test
    void testVnpayReturn_InvalidSignature() throws Exception {
        session.setAttribute("loggedInUser", mockUser);
        when(vnPayService.verifySignature(anyMap())).thenReturn(false);

        mockMvc.perform(get("/payment/vnpay-return")
                        .param("vnp_ResponseCode", "00")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/result"))
                .andExpect(flash().attribute("paymentSuccess", false))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // ===================================================
    // 7. GET /payment/vnpay-return — Thanh toán thất bại (responseCode != 00)
    // ===================================================
    @Test
    void testVnpayReturn_FailedPayment() throws Exception {
        session.setAttribute("loggedInUser", mockUser);
        when(vnPayService.verifySignature(anyMap())).thenReturn(true);
        when(vnPayService.isPaymentSuccess("24")).thenReturn(false);

        mockMvc.perform(get("/payment/vnpay-return")
                        .param("vnp_ResponseCode", "24")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/result"))
                .andExpect(flash().attribute("paymentSuccess", false))
                .andExpect(flash().attribute("responseCode", "24"));
    }

    // ===================================================
    // 8. GET /payment/result — Trang kết quả thanh toán
    // ===================================================
    @Test
    void testShowPaymentResult() throws Exception {
        session.setAttribute("loggedInUser", mockUser);

        mockMvc.perform(get("/payment/result").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("payment-result"))
                .andExpect(model().attributeExists("user"));
    }
}
