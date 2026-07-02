package com.haan.perfumeshop.service;

import com.haan.perfumeshop.model.*;
import com.haan.perfumeshop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private PerfumeRepository perfumeRepository;

    @Mock
    private CartService cartService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User mockUser;
    private Perfume mockPerfume;
    private PerfumeVariant mockVariant;
    private Cart mockCartItem;

    @BeforeEach
    void setUp() {
        // Tạo User giả lập
        mockUser = new User();
        mockUser.setId_user(1L);
        mockUser.setFullName("Nguyen Van Test");
        mockUser.setEmail("test@gmail.com");
        mockUser.setPhone("0987654321");

        // Tạo Variant giả lập
        mockVariant = new PerfumeVariant();
        mockVariant.setId_bien_the(10L);
        mockVariant.setDung_tich("50ml");
        mockVariant.setGia_ban("500000");
        mockVariant.setSo_luong_ton(50);

        // Tạo Perfume giả lập
        mockPerfume = new Perfume();
        mockPerfume.setId_nuoc_hoa(100L);
        mockPerfume.setTen_sp("Chanel No.5 Test");
        mockPerfume.setThuong_hieu("Chanel");
        mockPerfume.setTon_kho(100);
        List<PerfumeVariant> variants = new ArrayList<>();
        variants.add(mockVariant);
        mockPerfume.setVariants(variants);

        // Tạo Cart item giả lập
        mockCartItem = new Cart();
        mockCartItem.setId(1L);
        mockCartItem.setUser(mockUser);
        mockCartItem.setPerfume(mockPerfume);
        mockCartItem.setVariant(mockVariant);
        mockCartItem.setSo_luong(2);
    }

    @Test
    void testCheckoutOrder_Success_COD() throws Exception {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(cartService.getCartByUserId(1L)).thenReturn(List.of(mockCartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(999L);
            return order;
        });
        when(perfumeRepository.save(any(Perfume.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderDetailRepository.save(any(OrderDetail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order result = orderService.checkoutOrder(mockUser, "COD", null);

        // Assert
        assertNotNull(result);
        assertEquals("COD", result.getPhuong_thuc_thanh_toan());
        assertEquals("Pending", result.getTrang_thai());
        verify(cartService).clearCart(1L);
        verify(emailService).sendOrderConfirmationEmail(any(Order.class));
    }

    @Test
    void testCheckoutOrder_Success_VNPay() throws Exception {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(cartService.getCartByUserId(1L)).thenReturn(List.of(mockCartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(998L);
            return order;
        });
        when(perfumeRepository.save(any(Perfume.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderDetailRepository.save(any(OrderDetail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order result = orderService.checkoutOrder(mockUser, "VNPay", "TXN_123456");

        // Assert
        assertNotNull(result);
        assertEquals("VNPay", result.getPhuong_thuc_thanh_toan());
        assertEquals("TXN_123456", result.getMa_giao_dich());
    }

    @Test
    void testCheckoutOrder_EmptyCart_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(cartService.getCartByUserId(1L)).thenReturn(Collections.emptyList());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            orderService.checkoutOrder(mockUser, "COD", null);
        });
        assertTrue(exception.getMessage().contains("trống"));
    }

    @Test
    void testCheckoutOrder_InsufficientStock_Variant_ThrowsException() {
        // Arrange: Tồn kho variant chỉ còn 1 nhưng đặt 2
        mockVariant.setSo_luong_ton(1);
        mockCartItem.setSo_luong(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(cartService.getCartByUserId(1L)).thenReturn(List.of(mockCartItem));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            orderService.checkoutOrder(mockUser, "COD", null);
        });
        assertTrue(exception.getMessage().contains("không đủ số lượng"));
    }

    @Test
    void testCheckoutOrder_InsufficientStock_NullVariant_ThrowsException() {
        // Arrange: Sản phẩm không có biến thể, tồn kho gốc = 0
        Cart cartNoVariant = new Cart();
        cartNoVariant.setId(2L);
        cartNoVariant.setUser(mockUser);
        Perfume perfumeNoVariant = new Perfume();
        perfumeNoVariant.setId_nuoc_hoa(200L);
        perfumeNoVariant.setTen_sp("Perfume No Variant");
        perfumeNoVariant.setTon_kho(0);
        perfumeNoVariant.setVariants(new ArrayList<>());
        cartNoVariant.setPerfume(perfumeNoVariant);
        cartNoVariant.setVariant(null);
        cartNoVariant.setSo_luong(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(cartService.getCartByUserId(1L)).thenReturn(List.of(cartNoVariant));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            orderService.checkoutOrder(mockUser, "COD", null);
        });
        assertTrue(exception.getMessage().contains("không đủ số lượng"));
    }

    @Test
    void testCheckoutOrder_EmailFailure_StillSucceeds() throws Exception {
        // Arrange: Email service ném exception nhưng đơn hàng vẫn phải thành công
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(cartService.getCartByUserId(1L)).thenReturn(List.of(mockCartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(997L);
            return order;
        });
        when(perfumeRepository.save(any(Perfume.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderDetailRepository.save(any(OrderDetail.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("SMTP connection failed")).when(emailService).sendOrderConfirmationEmail(any());

        // Act — KHÔNG được ném exception
        Order result = orderService.checkoutOrder(mockUser, "COD", null);

        // Assert
        assertNotNull(result);
        assertEquals(997L, result.getId());
        verify(cartService).clearCart(1L);
    }

    @Test
    void testCheckoutOrder_ClearsCartAfterSuccess() throws Exception {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(cartService.getCartByUserId(1L)).thenReturn(List.of(mockCartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(996L);
            return order;
        });
        when(perfumeRepository.save(any(Perfume.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderDetailRepository.save(any(OrderDetail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderService.checkoutOrder(mockUser, "COD", null);

        // Assert: clearCart phải được gọi đúng 1 lần với userId = 1L
        verify(cartService, times(1)).clearCart(1L);
    }

    @Test
    void testCheckoutOrder_CalculatesTotalCorrectly() throws Exception {
        // Arrange: 2 cái x 500,000 VND = 1,000,000 VND
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(cartService.getCartByUserId(1L)).thenReturn(List.of(mockCartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(995L);
            return order;
        });
        when(perfumeRepository.save(any(Perfume.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderDetailRepository.save(any(OrderDetail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order result = orderService.checkoutOrder(mockUser, "COD", null);

        // Assert: Tổng tiền = 2 x 500000 = 1000000
        assertEquals(1000000.0, result.getTong_tien(), 0.01);
    }
}
