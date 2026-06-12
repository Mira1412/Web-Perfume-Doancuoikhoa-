package com.haan.perfumeshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn tuyệt đối tới thư mục 'uploads' ở gốc dự án
        String uploadPath = Paths.get(System.getProperty("user.dir"), "uploads").toUri().toString();
        
        // Đảm bảo đường dẫn luôn kết thúc bằng dấu '/' để Spring Boot hiểu đây là một thư mục
        if (!uploadPath.endsWith("/")) {
            uploadPath += "/";
        }
        
        // Mở khóa: Cứ có link nào trên web bắt đầu bằng /uploads/ thì sẽ chui vào thư mục này để lấy ảnh
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}