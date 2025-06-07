package com.budgetmate.receipt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                 // receipt-service 모든 엔드포인트
                .allowedOriginPatterns(
                        "http://localhost:5173",
                        "http://172.20.10.*:5173") // 모바일 IP 대역
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
