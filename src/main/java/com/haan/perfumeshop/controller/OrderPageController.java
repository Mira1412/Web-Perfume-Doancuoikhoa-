package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Order;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class OrderPageController {

    @Autowired
    private OrderRepository orderRepository; // Thêm Repository để gọi Database

    @GetMapping("/orders")
    public String showCustomerOrders(HttpSession session, Model model) {
        // 1. Kiểm tra đăng nhập
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // 2. Tìm toàn bộ đơn hàng của khách này trong Database
        List<Order> myOrders = orderRepository.findByUser_Id_user(currentUser.getId_user());

        // 3. Gửi danh sách qua file orders.html
        model.addAttribute("orders", myOrders);

        return "orders";
    }
}