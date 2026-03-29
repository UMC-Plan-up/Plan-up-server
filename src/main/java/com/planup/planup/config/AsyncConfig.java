package com.planup.planup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "pushExecutor")
    public Executor pushExecutor() {
            ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
            ex.setCorePoolSize(8);
            ex.setMaxPoolSize(32);
            ex.setQueueCapacity(500);
            ex.setThreadNamePrefix("push-");
            ex.initialize();
            return ex;
    }
}
