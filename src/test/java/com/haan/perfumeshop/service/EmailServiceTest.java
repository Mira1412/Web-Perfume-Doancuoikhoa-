package com.haan.perfumeshop.service;

import com.haan.perfumeshop.model.Order;
import com.haan.perfumeshop.model.OrderDetail;
import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.repository.OrderDetailRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @InjectMocks
    private EmailService emailService;

    private Order mockOrder;
    private User mockUser;
    private MimeMessage mockMimeMessage;

    @BeforeEach
    void setUp() {
        // Inject giá trị cho @Value field
        ReflectionTestUtils.setField(emailService, "senderEmail", "test-sender@gmail.com");

        mockUser = new User();
        mockUser.setId_user(1L);
        mockUser.setFullName("Nguyen Van A");
        mockUser.setEmail("customer@gmail.com");

        mockOrder = new Order();
        mockOrder.setId(100L);
        mockOrder.setUser(mockUser);
        mockOrder.setTong_tien(1500000.0);
        mockOrder.setNgay_dat(LocalDateTime.of(2026, 7, 2, 10, 30));
        mockOrder.setTrang_thai("Pending");

        // Mock MimeMessage
        mockMimeMessage = mock(MimeMessage.class);
    }

    // ===================================================
    // Test gửi email xác nhận đơn hàng thành công (HTML)
    // ===================================================
    @Test
    void testSendOrderConfirmationEmail_Success() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(orderDetailRepository.findByOrder_Id(100L)).thenReturn(Collections.emptyList());
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendOrderConfirmationEmail(mockOrder);

        // Verify: mailSender.send() phải được gọi 1 lần với MimeMessage
        verify(mailSender, times(1)).send(eq(mockMimeMessage));
        verify(orderDetailRepository, times(1)).findByOrder_Id(100L);
    }

    // ===================================================
    // Test: User null → bỏ qua, KHÔNG gửi email
    // ===================================================
    @Test
    void testSendOrderConfirmationEmail_NullUser_SkipsSending() {
        Order orderNoUser = new Order();
        orderNoUser.setId(200L);
        orderNoUser.setUser(null);

        emailService.sendOrderConfirmationEmail(orderNoUser);

        // Verify: mailSender KHÔNG được gọi
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    // ===================================================
    // Test: Email null → bỏ qua, KHÔNG gửi
    // ===================================================
    @Test
    void testSendOrderConfirmationEmail_NullEmail_SkipsSending() {
        User userNoEmail = new User();
        userNoEmail.setId_user(2L);
        userNoEmail.setEmail(null);

        Order orderNoEmail = new Order();
        orderNoEmail.setId(201L);
        orderNoEmail.setUser(userNoEmail);

        emailService.sendOrderConfirmationEmail(orderNoEmail);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    // ===================================================
    // Test gửi email khôi phục mật khẩu thành công (HTML)
    // ===================================================
    @Test
    void testSendForgotPasswordEmail_Success() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendForgotPasswordEmail("forgot@gmail.com", "TempPass123");

        verify(mailSender, times(1)).send(eq(mockMimeMessage));
    }

    // ===================================================
    // Test gửi email thông báo hủy đơn hàng (thông qua update email)
    // ===================================================
    @Test
    void testSendOrderCancellationEmail_Success() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(orderDetailRepository.findByOrder_Id(100L)).thenReturn(Collections.emptyList());
        doNothing().when(mailSender).send(any(MimeMessage.class));
        
        mockOrder.setTrang_thai("Cancelled");

        emailService.sendOrderCancellationEmail(mockOrder);

        verify(mailSender, times(1)).send(eq(mockMimeMessage));
    }

    // ===================================================
    // Test gửi email cập nhật trạng thái khác (Packing)
    // ===================================================
    @Test
    void testSendOrderStatusUpdateEmail_Packing() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(orderDetailRepository.findByOrder_Id(100L)).thenReturn(Collections.emptyList());
        doNothing().when(mailSender).send(any(MimeMessage.class));
        
        mockOrder.setTrang_thai("Packing");

        emailService.sendOrderStatusUpdateEmail(mockOrder);

        verify(mailSender, times(1)).send(eq(mockMimeMessage));
    }
}
