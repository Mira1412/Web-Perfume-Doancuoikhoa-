package com.haan.perfumeshop.service;

import com.haan.perfumeshop.model.Cart;
import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.model.PerfumeVariant;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.repository.CartRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    private User mockUser;
    private Perfume mockPerfume;
    private PerfumeVariant mockVariant;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId_user(1L);
        mockUser.setFullName("Test User");
        mockUser.setEmail("test@gmail.com");

        mockVariant = new PerfumeVariant();
        mockVariant.setId_bien_the(10L);
        mockVariant.setDung_tich("50ml");
        mockVariant.setGia_ban("500000");

        mockPerfume = new Perfume();
        mockPerfume.setId_nuoc_hoa(100L);
        mockPerfume.setTen_sp("Test Perfume");
    }

    // ===================================================
    // Test thêm sản phẩm mới vào giỏ hàng
    // ===================================================
    @Test
    void testAddToCart_NewItem() {
        // Arrange: Giỏ hàng trống, thêm mới
        Cart newCart = new Cart();
        newCart.setUser(mockUser);
        newCart.setPerfume(mockPerfume);
        newCart.setVariant(mockVariant);
        newCart.setSo_luong(1);

        when(cartRepository.findByUser_Id_user(1L)).thenReturn(Collections.emptyList());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart saved = invocation.getArgument(0);
            saved.setId(999L);
            return saved;
        });

        // Act
        Cart result = cartService.addToCart(newCart);

        // Assert
        assertNotNull(result);
        assertEquals(999L, result.getId());
        verify(cartRepository).save(newCart);
    }

    // ===================================================
    // Test thêm trùng sản phẩm + cùng dung tích → cộng dồn
    // ===================================================
    @Test
    void testAddToCart_DuplicateItem_IncrementsQuantity() {
        // Arrange: Giỏ đã có 1 cái cùng perfume + cùng variant
        Cart existingItem = new Cart();
        existingItem.setId(50L);
        existingItem.setUser(mockUser);
        existingItem.setPerfume(mockPerfume);
        existingItem.setVariant(mockVariant);
        existingItem.setSo_luong(2);

        Cart newItem = new Cart();
        newItem.setUser(mockUser);
        newItem.setPerfume(mockPerfume);
        newItem.setVariant(mockVariant);
        newItem.setSo_luong(3);

        when(cartRepository.findByUser_Id_user(1L)).thenReturn(List.of(existingItem));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Cart result = cartService.addToCart(newItem);

        // Assert: Số lượng phải cộng dồn = 2 + 3 = 5
        assertEquals(5, result.getSo_luong());
        assertEquals(50L, result.getId()); // Cùng record cũ, không tạo mới
    }

    // ===================================================
    // Test cùng sản phẩm nhưng khác dung tích → tạo dòng mới
    // ===================================================
    @Test
    void testAddToCart_SameProductDifferentVariant_CreatesNew() {
        // Arrange: Giỏ có "50ml", giờ thêm "100ml"
        Cart existingItem = new Cart();
        existingItem.setId(50L);
        existingItem.setUser(mockUser);
        existingItem.setPerfume(mockPerfume);
        existingItem.setVariant(mockVariant); // 50ml, id=10

        PerfumeVariant differentVariant = new PerfumeVariant();
        differentVariant.setId_bien_the(20L); // ID khác
        differentVariant.setDung_tich("100ml");

        Cart newItem = new Cart();
        newItem.setUser(mockUser);
        newItem.setPerfume(mockPerfume);
        newItem.setVariant(differentVariant);
        newItem.setSo_luong(1);

        when(cartRepository.findByUser_Id_user(1L)).thenReturn(List.of(existingItem));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart saved = invocation.getArgument(0);
            saved.setId(51L); // ID mới
            return saved;
        });

        // Act
        Cart result = cartService.addToCart(newItem);

        // Assert: Phải tạo dòng mới, không cộng dồn
        assertEquals(1, result.getSo_luong());
        verify(cartRepository).save(newItem);
    }

    // ===================================================
    // Test thêm mà không có user → ném exception
    // ===================================================
    @Test
    void testAddToCart_NullUser_ThrowsException() {
        Cart badCart = new Cart();
        badCart.setUser(null);
        badCart.setPerfume(mockPerfume);
        badCart.setSo_luong(1);

        assertThrows(IllegalArgumentException.class, () -> {
            cartService.addToCart(badCart);
        });
    }

    // ===================================================
    // Test cập nhật số lượng thành công
    // ===================================================
    @Test
    void testUpdateQuantity_Success() {
        Cart existingCart = new Cart();
        existingCart.setId(50L);
        existingCart.setSo_luong(2);

        when(cartRepository.findById(50L)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.updateQuantity(50L, 5);

        assertNotNull(result);
        assertEquals(5, result.getSo_luong());
    }

    // ===================================================
    // Test cập nhật với số lượng <= 0 → không cập nhật
    // ===================================================
    @Test
    void testUpdateQuantity_InvalidQuantity() {
        Cart existingCart = new Cart();
        existingCart.setId(50L);
        existingCart.setSo_luong(2);

        when(cartRepository.findById(50L)).thenReturn(Optional.of(existingCart));

        // Số lượng <= 0 → trả về cart cũ, không thay đổi
        Cart result = cartService.updateQuantity(50L, 0);
        assertEquals(2, result.getSo_luong());
        verify(cartRepository, never()).save(any());
    }

    // ===================================================
    // Test xóa item khỏi giỏ
    // ===================================================
    @Test
    void testRemoveFromCart() {
        doNothing().when(cartRepository).deleteById(50L);

        cartService.removeFromCart(50L);

        verify(cartRepository, times(1)).deleteById(50L);
    }

    // ===================================================
    // Test xóa sạch giỏ hàng
    // ===================================================
    @Test
    void testClearCart() {
        Cart item1 = new Cart();
        item1.setId(1L);
        Cart item2 = new Cart();
        item2.setId(2L);

        when(cartRepository.findByUser_Id_user(1L)).thenReturn(List.of(item1, item2));
        doNothing().when(cartRepository).deleteAll(any());

        cartService.clearCart(1L);

        verify(cartRepository).findByUser_Id_user(1L);
        verify(cartRepository).deleteAll(List.of(item1, item2));
    }
}
