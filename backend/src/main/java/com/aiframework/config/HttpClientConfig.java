package com.aiframework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for HTTP clients and external API integration
 */
@Configuration
public class HttpClientConfig {

    /**
     * RestTemplate bean for making HTTP requests to external APIs
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
