package com.haan.perfumeshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // Kích hoạt gửi email bất đồng bộ
public class PerfumeThesisApplication {

	public static void main(String[] args) {
		SpringApplication.run(PerfumeThesisApplication.class, args);
	}

}
	