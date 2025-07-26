package com.aiframework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Centralized Web configuration for CORS and other web-related settings
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:3000,http://127.0.0.1:3000,https://via.placeholder.com}")
    private String allowedOriginsString;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOriginsString.split(",");

        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Cache preflight for 1 hour
    }

    /**
     * Alternative CORS configuration bean for more complex scenarios
     * This works in conjunction with Spring Security
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow origins from configuration plus external image providers
        String[] origins = allowedOriginsString.split(",");
        List<String> allowedOrigins = Arrays.asList(origins);
        configuration.setAllowedOrigins(allowedOrigins);
        
        // Also allow common image CDNs and placeholder services
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*", 
            "https://via.placeholder.com",
            "https://*.placeholder.com",
            "https://picsum.photos",
            "https://*.picsum.photos"
        ));

        // Allow all common HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));

        // Specify allowed headers explicitly instead of using "*"
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "Accept",
            "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers",
            "Cache-Control", "Pragma", "X-Content-Type-Options", "X-XSS-Protection",
            "X-Frame-Options", "sec-ch-ua", "sec-ch-ua-mobile", "sec-ch-ua-platform"
        ));

        // Allow credentials (cookies, authorization headers, etc.)
        configuration.setAllowCredentials(true);

        // Expose common headers to the frontend
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "Accept",
            "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers",
            "X-Total-Count", "X-Page-Count", "Link"
        ));

        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
