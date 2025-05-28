package com.budgetmate.receipt.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.HandlerStrategies;

@Configuration
public class MultipartConfig {
    @Bean
    public HandlerStrategies handlerStrategies() {
        return HandlerStrategies.builder()
                .codecs(config -> config.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }
}
