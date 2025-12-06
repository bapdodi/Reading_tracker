package com.readingtracker.agent;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Installer component that triggers the ByteBuddy agent installation early in Spring startup.
 */
@Component
public class CacheEntityAgentInstaller {

    @PostConstruct
    public void init() {
        // Install agent and transformer
        CacheEntityAgent.installAgent();
    }
}
