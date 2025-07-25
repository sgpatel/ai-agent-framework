package com.aiframework.controller;

import com.aiframework.service.LocalLLMService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;

@RestController
@RequestMapping("/api/llm")
@CrossOrigin(origins = "*")
public class LLMController {

    private static final Logger logger = LoggerFactory.getLogger(LLMController.class);

    @Autowired
    private LocalLLMService localLLMService;

    @PostMapping("/generate")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> generateResponse(@RequestBody Map<String, Object> request) {
        String prompt = (String) request.get("prompt");
        Integer maxTokens = (Integer) request.getOrDefault("maxTokens", 1024);
        Double temperature = (Double) request.getOrDefault("temperature", 0.7);

        if (prompt == null || prompt.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest().body(Map.of("error", "Prompt is required"))
            );
        }

        return localLLMService.generateResponse(prompt, maxTokens, temperature.floatValue())
            .thenApply(response -> ResponseEntity.ok(Map.<String, Object>of(
                "response", response,
                "model", "GPT4All",
                "status", "success"
            )))
            .exceptionally(throwable -> {
                logger.error("Error generating LLM response", throwable);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Failed to generate response");
                errorResponse.put("message", throwable.getMessage());
                return ResponseEntity.internalServerError().body(errorResponse);
            });
    }

    @PostMapping("/analyze-stock")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> analyzeStock(@RequestBody Map<String, String> request) {
        String symbol = request.get("symbol");
        String marketData = request.get("marketData");
        String technicalIndicators = request.get("technicalIndicators");

        if (symbol == null || marketData == null) {
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest().body(Map.of("error", "Symbol and marketData are required"))
            );
        }

        return localLLMService.analyzeStockData(symbol, marketData, technicalIndicators)
            .thenApply(analysis -> ResponseEntity.ok(Map.<String, Object>of(
                "analysis", analysis,
                "symbol", symbol,
                "timestamp", System.currentTimeMillis()
            )))
            .exceptionally(throwable -> {
                logger.error("Error analyzing stock data", throwable);
                return ResponseEntity.internalServerError().body(Map.<String, Object>of(
                    "error", "Failed to analyze stock data",
                    "message", throwable.getMessage()
                ));
            });
    }

    @PostMapping("/trading-insight")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getTradingInsight(@RequestBody Map<String, String> request) {
        String symbol = request.get("symbol");
        String priceData = request.get("priceData");
        String signals = request.get("signals");

        return localLLMService.generateTradingInsight(symbol, priceData, signals)
            .thenApply(insight -> ResponseEntity.ok(Map.<String, Object>of(
                "insight", insight,
                "symbol", symbol,
                "type", "trading_insight"
            )))
            .exceptionally(throwable -> {
                logger.error("Error generating trading insight", throwable);
                return ResponseEntity.internalServerError().body(Map.<String, Object>of(
                    "error", "Failed to generate trading insight",
                    "message", throwable.getMessage()
                ));
            });
    }

    @PostMapping("/coordinate-agents")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> coordinateAgents(@RequestBody Map<String, String> request) {
        String taskDescription = request.get("taskDescription");
        String availableAgents = request.get("availableAgents");
        String currentContext = request.get("currentContext");

        return localLLMService.coordinateAgents(taskDescription, availableAgents, currentContext)
            .thenApply(coordination -> ResponseEntity.ok(Map.<String, Object>of(
                "coordination", coordination,
                "task", taskDescription,
                "status", "coordinated"
            )))
            .exceptionally(throwable -> {
                logger.error("Error coordinating agents", throwable);
                return ResponseEntity.internalServerError().body(Map.<String, Object>of(
                    "error", "Failed to coordinate agents",
                    "message", throwable.getMessage()
                ));
            });
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
            "status", localLLMService.getModelStatus(),
            "available", localLLMService.isAvailable(),
            "model", "GPT4All Llama 3.2 3B Instruct"
        ));
    }
}
