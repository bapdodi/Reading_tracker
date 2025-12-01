package com.readingtracker.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * JPA Auditing 설정
 * 
 * @CreatedDate와 @LastModifiedDate가 한국 시간대(Asia/Seoul) 기준으로 설정되도록 합니다.
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
public class JpaConfig {
    
    /**
     * 한국 시간대 기준 DateTimeProvider
     * 
     * @CreatedDate와 @LastModifiedDate가 한국 시간대 기준으로 설정되도록 합니다.
     */
    @Bean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
    }
}

