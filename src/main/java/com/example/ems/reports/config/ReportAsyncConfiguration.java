package com.example.ems.reports.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ReportAsyncConfiguration {

    @Autowired
    private ReportStorageProperties properties;

    @Bean(name = "reportExecutor")
    public Executor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getAsync().getPoolSize());
        executor.setMaxPoolSize(properties.getAsync().getPoolSize() * 2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Report-Async-");
        executor.initialize();
        return executor;
    }
}
