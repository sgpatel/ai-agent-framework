package com.aiframework.config;

import com.aiframework.security.JwtAuthenticationFilter;
import com.aiframework.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Enhanced Security configuration with JWT authentication
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for JWT authentication
            .csrf(csrf -> csrf.disable())

            // Use centralized CORS configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // Configure authorization rules - more permissive for development
            .authorizeHttpRequests(authz -> authz
                // Allow public access to authentication endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // Allow public access to health check and actuator endpoints
                .requestMatchers("/actuator/**", "/health/**").permitAll()
                // Allow public access to H2 console for development
                .requestMatchers("/h2-console/**").permitAll()
                // Allow public access to stock endpoints
                .requestMatchers("/api/stocks/**").permitAll()
                // Allow public access to agent endpoints
                .requestMatchers("/api/agents/**").permitAll()
                // Allow public access to WebSocket endpoints
                .requestMatchers("/ws/**", "/websocket/**").permitAll()
                // Allow public access to tasks and metrics
                .requestMatchers("/api/tasks/**").permitAll()
                .requestMatchers("/api/metrics/**").permitAll()
                .requestMatchers("/api/plugins/**").permitAll()
                // Allow OPTIONS requests for CORS preflight
                .requestMatchers("OPTIONS", "/**").permitAll()
                // Allow all other requests for development - this should fix 403 issues
                .anyRequest().permitAll()
            )

            // Configure session management for JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Set authentication provider
            .authenticationProvider(authenticationProvider())

            // Add JWT authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // Allow frames for H2 console and disable additional security headers that might cause issues
            .headers(headers -> headers
                .frameOptions().disable()
                .contentTypeOptions().disable()
                .httpStrictTransportSecurity().disable()
            );

        return http.build();
    }
}
