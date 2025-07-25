package com.aiframework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AiAgentFrameworkApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiAgentFrameworkApplication.class, args);
    }
}
