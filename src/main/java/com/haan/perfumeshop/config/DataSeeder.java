package com.haan.perfumeshop.config;

import com.haan.perfumeshop.model.User; // Nhớ import đúng class User của bạn
import com.haan.perfumeshop.repository.UserRepository; // Nhớ import UserRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {

        if (userRepository.findByEmail("admin@haan.vn") == null) {
            User admin = new User();
            admin.setEmail("admin@haan.vn");
            admin.setPassword("123456");
            admin.setRole("ADMIN"); // Gắn quyền Admin

            userRepository.save(admin);
            System.out.println("✅ Đã khởi tạo tài khoản Admin mặc định!");
        }
    }
}