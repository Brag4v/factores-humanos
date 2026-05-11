package com.cms.contract.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                5, TimeUnit.SECONDS,  // Connect timeout
                10, TimeUnit.SECONDS, // Read timeout
                true                   // Follow redirects
        );
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
                100,  // Initial interval in ms
                1000, // Max interval in ms
                3     // Max attempts
        );
    }
}
