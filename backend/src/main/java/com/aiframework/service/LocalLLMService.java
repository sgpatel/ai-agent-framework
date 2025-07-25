package com.aiframework.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class LocalLLMService {
    private static final Logger logger = LoggerFactory.getLogger(LocalLLMService.class);

    @Value("${llm.local.api.url:http://localhost:4891/v1}")
    private String apiUrl;

    @Value("${llm.local.api.enabled:true}")
    private boolean enabled;

    @Value("${llm.max.tokens:1024}")
    private int maxTokens;

    @Value("${llm.temperature:0.7}")
    private float temperature;

    @Value("${llm.local.api.timeout:30000}")
    private int timeoutMs;

    private WebClient webClient;
    private boolean isApiAvailable = false;

    @PostConstruct
    public void init() {
        if (!enabled) {
            logger.info("Local LLM API service is disabled");
            return;
        }

        this.webClient = WebClient.builder()
            .baseUrl(apiUrl)
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();

        // Test API availability
        checkApiAvailability();
    }

    private void checkApiAvailability() {
        try {
            webClient.get()
                .uri("/models")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(5000))
                .block();

            isApiAvailable = true;
            logger.info("GPT4All local API is available at: {}", apiUrl);
        } catch (Exception e) {
            logger.warn("GPT4All local API not available at: {} - {}", apiUrl, e.getMessage());
            isApiAvailable = false;
        }
    }

    public CompletableFuture<String> generateResponse(String prompt) {
        return generateResponse(prompt, maxTokens, temperature);
    }

    public CompletableFuture<String> generateResponse(String prompt, int tokens, float temp) {
        if (!enabled || !isApiAvailable) {
            return CompletableFuture.completedFuture("Local GPT4All API not available. Please ensure GPT4All is running locally.");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Generating response for prompt: {}", prompt.substring(0, Math.min(50, prompt.length())));

                Map<String, Object> requestBody = Map.of(
                    "model", "gpt4all",
                    "messages", new Object[]{
                        Map.of("role", "user", "content", prompt)
                    },
                    "max_tokens", tokens,
                    "temperature", temp,
                    "stream", false
                );

                Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

                if (response != null && response.containsKey("choices")) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    if (!choices.isEmpty()) {
                        Map<String, Object> choice = choices.get(0);
                        Map<String, Object> message = (Map<String, Object>) choice.get("message");
                        String content = (String) message.get("content");

                        logger.debug("Generated response length: {} characters", content.length());
                        return content.trim();
                    }
                }

                return "No response generated";

            } catch (WebClientException e) {
                logger.error("Error calling GPT4All API: {}", e.getMessage());
                return "Error generating response: " + e.getMessage();
            } catch (Exception e) {
                logger.error("Unexpected error generating LLM response: {}", e.getMessage());
                return "Error generating response: " + e.getMessage();
            }
        });
    }

    public CompletableFuture<String> analyzeStockData(String symbol, String marketData, String technicalIndicators) {
        String prompt = buildStockAnalysisPrompt(symbol, marketData, technicalIndicators);
        return generateResponse(prompt, 512, 0.3f); // Lower temperature for more consistent analysis
    }

    public CompletableFuture<String> generateTradingInsight(String symbol, String priceData, String signals) {
        String prompt = buildTradingInsightPrompt(symbol, priceData, signals);
        return generateResponse(prompt, 256, 0.5f);
    }

    public CompletableFuture<String> coordinateAgents(String taskDescription, String availableAgents, String currentContext) {
        String prompt = buildAgentCoordinationPrompt(taskDescription, availableAgents, currentContext);
        return generateResponse(prompt, 384, 0.4f);
    }

    private String buildStockAnalysisPrompt(String symbol, String marketData, String technicalIndicators) {
        return String.format("""
            You are a financial analyst AI. Analyze the following stock data for %s:
            
            Market Data: %s
            Technical Indicators: %s
            
            Provide a concise analysis covering:
            1. Current market sentiment
            2. Key technical signals
            3. Risk factors
            4. Short-term outlook
            
            Response format: JSON with fields: sentiment, signals, risks, outlook
            """, symbol, marketData, technicalIndicators);
    }

    private String buildTradingInsightPrompt(String symbol, String priceData, String signals) {
        return String.format("""
            As a trading advisor AI, analyze %s with the following data:
            
            Price Data: %s
            Trading Signals: %s
            
            Provide trading insights:
            1. Entry/exit recommendations
            2. Risk management advice
            3. Position sizing suggestions
            
            Keep response under 150 words and actionable.
            """, symbol, priceData, signals);
    }

    private String buildAgentCoordinationPrompt(String taskDescription, String availableAgents, String currentContext) {
        return String.format("""
            You are an AI coordinator managing multiple specialized agents. 
            
            Task: %s
            Available Agents: %s
            Current Context: %s
            
            Determine:
            1. Which agents should handle this task
            2. In what order should they execute
            3. What information should be shared between agents
            4. Expected coordination strategy
            
            Response format: JSON with fields: selected_agents, execution_order, shared_data, strategy
            """, taskDescription, availableAgents, currentContext);
    }

    public boolean isAvailable() {
        return enabled && isApiAvailable;
    }

    public String getModelStatus() {
        if (!enabled) return "DISABLED";
        if (!isApiAvailable) return "LOADING";
        return "READY";
    }
}
