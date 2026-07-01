package com.haan.perfumeshop.service;

import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId_user(1L);
        mockUser.setFullName("Nguyen Van A");
        mockUser.setEmail("test@gmail.com");
        mockUser.setPhone("0987654321");
        mockUser.setPassword("password123");
        mockUser.setRole("customer");
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        when(userRepository.findByEmail("newuser@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId_user(2L);
            return user;
        });

        User registeredUser = userService.registerUser(
                "New User",
                "newuser@gmail.com",
                "0123456789",
                "password123",
                "password123"
        );

        assertNotNull(registeredUser);
        assertEquals(2L, registeredUser.getId_user());
        assertEquals("New User", registeredUser.getFullName()); // Service trim & lowercase trong code thực tế nếu có
        assertEquals("newuser@gmail.com", registeredUser.getEmail());
        assertEquals("customer", registeredUser.getRole());
    }

    @Test
    void testRegisterUser_PasswordNotMatch() {
        Exception exception = assertThrows(Exception.class, () -> {
            userService.registerUser(
                    "New User",
                    "newuser@gmail.com",
                    "0123456789",
                    "password123",
                    "different_password"
            );
        });

        assertEquals("Mật khẩu xác nhận không khớp, vui lòng kiểm tra lại!", exception.getMessage());
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));

        Exception exception = assertThrows(Exception.class, () -> {
            userService.registerUser(
                    "Nguyen Van A",
                    "test@gmail.com",
                    "0987654321",
                    "password123",
                    "password123"
            );
        });

        assertTrue(exception.getMessage().contains("đã được đăng ký"));
    }

    @Test
    void testLoginUser_Success() throws Exception {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));

        User loggedInUser = userService.loginUser("test@gmail.com", "password123");

        assertNotNull(loggedInUser);
        assertEquals("test@gmail.com", loggedInUser.getEmail());
        assertEquals("Nguyen Van A", loggedInUser.getFullName());
    }

    @Test
    void testLoginUser_WrongPassword() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));

        Exception exception = assertThrows(Exception.class, () -> {
            userService.loginUser("test@gmail.com", "wrong_password");
        });

        assertEquals("Sai địa chỉ email hoặc mật khẩu. Vui lòng kiểm tra lại!", exception.getMessage());
    }
}
