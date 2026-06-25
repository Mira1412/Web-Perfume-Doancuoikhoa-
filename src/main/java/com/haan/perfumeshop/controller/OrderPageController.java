package com.haan.perfumeshop.controller;

import com.haan.perfumeshop.model.Order;
import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OrderPageController {

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/orders")
    public String showCustomerOrders(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            HttpSession session, Model model) {
        // 1. Kiểm tra đăng nhập
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (size <= 0) size = 10;
        if (page < 0) page = 0;

        // 2. Tìm đơn hàng của khách (có phân trang)
        Page<Order> orderPage = orderRepository.findByUser_Id_userPageable(
                currentUser.getId_user(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));

        // 3. Gửi danh sách qua file orders.html
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", orderPage.getNumber());
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "orders";
    }
}