package com.readingtracker.server.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * 서버 타임존 설정
 * 
 * 한국 시간대(Asia/Seoul, UTC+9)를 기본 타임존으로 설정합니다.
 * 모든 LocalDateTime.now() 호출이 한국 시간대 기준으로 동작하도록 합니다.
 */
@Configuration
public class TimeZoneConfig {
    
    @PostConstruct
    public void setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}

