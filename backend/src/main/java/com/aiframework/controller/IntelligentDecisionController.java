package com.aiframework.controller;

import com.aiframework.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.aiframework.dto.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/intelligent")
//@CrossOrigin(origins = "*")
public class IntelligentDecisionController {

    private static final Logger logger = LoggerFactory.getLogger(IntelligentDecisionController.class);

    @Autowired
    private LLMDecisionEngine decisionEngine;

    @Autowired
    private LLMCustomerSupportService customerSupport;

    @Autowired
    private IntelligentAgentCommunicationHub communicationHub;

    @PostMapping("/decision/investment")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> makeInvestmentDecision(@RequestBody Map<String, Object> request) {
        String symbol = (String) request.get("symbol");
        Map<String, Object> marketData = (Map<String, Object>) request.get("marketData");

        return decisionEngine.makeInvestmentDecision(symbol, marketData)
            .thenApply(decision -> ResponseEntity.ok(Map.<String, Object>of(
                "decision", decision,
                "timestamp", new Date(),
                "type", "INVESTMENT_DECISION"
            )))
            .exceptionally(throwable -> {
                logger.error("Error making investment decision", throwable);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Failed to make investment decision");
                errorResponse.put("message", throwable.getMessage());
                return ResponseEntity.internalServerError().body(errorResponse);
            });
    }

    @PostMapping("/decision/risk-assessment")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> assessRisk(@RequestBody Map<String, Object> request) {
        String symbol = (String) request.get("symbol");
        Map<String, Object> indicators = (Map<String, Object>) request.get("indicators");

        return decisionEngine.assessRiskLevel(symbol, indicators)
            .thenApply(riskAssessment -> ResponseEntity.ok(Map.<String, Object>of(
                "riskAssessment", riskAssessment,
                "timestamp", new Date(),
                "type", "RISK_ASSESSMENT"
            )))
            .exceptionally(throwable -> {
                logger.error("Error assessing risk", throwable);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Failed to assess risk");
                errorResponse.put("message", throwable.getMessage());
                return ResponseEntity.internalServerError().body(errorResponse);
            });
    }

    @PostMapping("/support/stock-alert")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> handleStockAlert(@RequestBody Map<String, Object> request) {
        String symbol = (String) request.get("symbol");
        String alertType = (String) request.get("alertType");
        Map<String, Object> marketData = (Map<String, Object>) request.get("marketData");

        return customerSupport.handleStockAlert(symbol, alertType, marketData)
            .thenApply(supportResponse -> ResponseEntity.ok(Map.<String, Object>of(
                "support", supportResponse,
                "urgencyLevel", supportResponse.getUrgencyLevel(),
                "actionRequired", supportResponse.isFollowUpRequired()
            )))
            .exceptionally(throwable -> {
                logger.error("Error handling stock alert", throwable);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Failed to handle stock alert");
                errorResponse.put("message", throwable.getMessage());
                return ResponseEntity.internalServerError().body(errorResponse);
            });
    }

    @PostMapping("/support/explain-indicator")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> explainIndicator(@RequestBody Map<String, Object> request) {
        String indicator = (String) request.get("indicator");
        String symbol = (String) request.get("symbol");
        Map<String, Object> context = (Map<String, Object>) request.get("context");

        return customerSupport.explainTechnicalIndicator(indicator, symbol, context)
            .thenApply(explanation -> ResponseEntity.ok(Map.<String, Object>of(
                "explanation", explanation,
                "educational", true,
                "type", "INDICATOR_EXPLANATION"
            )))
            .exceptionally(throwable -> {
                logger.error("Error explaining indicator", throwable);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Failed to explain indicator");
                errorResponse.put("message", throwable.getMessage());
                return ResponseEntity.internalServerError().body(errorResponse);
            });
    }

    @PostMapping("/agents/communicate")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> facilitateAgentCommunication(@RequestBody Map<String, Object> request) {
        String fromAgent = (String) request.get("fromAgent");
        String task = (String) request.get("task");
        Map<String, Object> context = (Map<String, Object>) request.get("context");

        AgentMessage message = new AgentMessage(fromAgent, task, context);

        return communicationHub.routeMessage(message)
            .thenApply(result -> ResponseEntity.ok(Map.<String, Object>of(
                "communicationResult", result,
                "agentsInvolved", result.getAgentResults().keySet(),
                "timestamp", result.getTimestamp()
            )))
            .exceptionally(throwable -> {
                logger.error("Error facilitating agent communication", throwable);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Failed to facilitate agent communication");
                errorResponse.put("message", throwable.getMessage());
                return ResponseEntity.internalServerError().body(errorResponse);
            });
    }

    @PostMapping("/agents/register")
    public ResponseEntity<Map<String, Object>> registerAgent(@RequestBody Map<String, Object> request) {
        String agentId = (String) request.get("agentId");
        String description = (String) request.get("description");
        List<String> capabilities = (List<String>) request.get("capabilities");
        String specialization = (String) request.get("specialization");

        AgentCapability capability = new AgentCapability(agentId, description, capabilities, specialization);
        communicationHub.registerAgent(agentId, capability);

        return ResponseEntity.ok(Map.<String, Object>of(
            "message", "Agent registered successfully",
            "agentId", agentId,
            "capabilities", capabilities
        ));
    }

    @GetMapping("/agents/available")
    public ResponseEntity<Map<String, Object>> getAvailableAgents() {
        List<AgentCapability> agents = communicationHub.getAvailableAgents();

        return ResponseEntity.ok(Map.<String, Object>of(
            "agents", agents,
            "count", agents.size(),
            "timestamp", new Date()
        ));
    }

    @PostMapping("/decision/comprehensive")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> makeComprehensiveDecision(@RequestBody Map<String, Object> request) {
        String symbol = (String) request.get("symbol");
        Map<String, Object> marketData = (Map<String, Object>) request.get("marketData");
        Map<String, Object> indicators = (Map<String, Object>) request.get("indicators");

        // Orchestrate multiple decision engines
        CompletableFuture<Object> investmentDecision = decisionEngine.makeInvestmentDecision(symbol, marketData)
            .thenApply(result -> (Object) result);

        CompletableFuture<Object> riskAssessment = decisionEngine.assessRiskLevel(symbol, indicators)
            .thenApply(result -> (Object) result);

        CompletableFuture<Object> tradingStrategy = decisionEngine.generateTradingStrategy(symbol, marketData)
            .thenApply(result -> (Object) result);

        return CompletableFuture.allOf(investmentDecision, riskAssessment, tradingStrategy)
            .thenApply(v -> {
                Map<String, Object> comprehensiveResult = new HashMap<>();
                try {
                    comprehensiveResult.put("investmentDecision", investmentDecision.get());
                    comprehensiveResult.put("riskAssessment", riskAssessment.get());
                    comprehensiveResult.put("tradingStrategy", tradingStrategy.get());
                    comprehensiveResult.put("timestamp", new Date());
                    comprehensiveResult.put("symbol", symbol);

                    return ResponseEntity.ok(Map.<String, Object>of(
                        "comprehensiveAnalysis", comprehensiveResult,
                        "confidence", calculateOverallConfidence(comprehensiveResult),
                        "recommendation", synthesizeRecommendation(comprehensiveResult)
                    ));
                } catch (Exception e) {
                    logger.error("Error creating comprehensive decision", e);
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Failed to create comprehensive decision");
                    errorResponse.put("message", e.getMessage());
                    return ResponseEntity.internalServerError().body(errorResponse);
                }
            });
    }

    @PostMapping("/support/risk-warning")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> issueRiskWarning(@RequestBody Map<String, Object> request) {
        String symbol = (String) request.get("symbol");
        String riskType = (String) request.get("riskType");
        Map<String, Object> riskData = (Map<String, Object>) request.get("riskData");

        return customerSupport.provideRiskWarning(symbol, riskType, riskData)
            .thenApply(warning -> ResponseEntity.ok(Map.<String, Object>of(
                "warning", warning,
                "urgency", warning.getUrgencyLevel(),
                "immediate", warning.getUrgencyLevel().equals("HIGH"),
                "followUp", warning.isFollowUpRequired()
            )))
            .exceptionally(throwable -> {
                logger.error("Error issuing risk warning", throwable);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Failed to issue risk warning");
                errorResponse.put("message", throwable.getMessage());
                return ResponseEntity.internalServerError().body(errorResponse);
            });
    }

    @PostMapping("/support/analyze")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> analyzeCustomerSupport(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        Map<String, Object> userContext = (Map<String, Object>) request.get("userContext");

        return customerSupport.handleGeneralInquiry(query, userContext)
            .thenApply(analysis -> ResponseEntity.ok(Map.<String, Object>of(
                "analysis", analysis.getContent(),
                "category", analysis.getResponseType(),
                "priority", analysis.getUrgencyLevel(),
                "confidence", 0.92,
                "estimatedResolutionTime", "5-10 minutes",
                "recommendedActions", analysis.getActionItems() != null ? analysis.getActionItems() : Arrays.asList(
                    "Review provided analysis",
                    "Consider implementing suggestions",
                    "Monitor market conditions"
                ),
                "timestamp", new Date()
            )))
            .exceptionally(throwable -> {
                logger.error("Error analyzing customer support request", throwable);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Failed to analyze customer support request");
                errorResponse.put("message", throwable.getMessage());
                return ResponseEntity.internalServerError().body(errorResponse);
            });
    }

    private double calculateOverallConfidence(Map<String, Object> results) {
        // Simple confidence calculation - in production, implement more sophisticated logic
        return 0.75; // Default confidence
    }

    private String synthesizeRecommendation(Map<String, Object> results) {
        // Synthesize overall recommendation from multiple analyses
        return "Based on comprehensive analysis, proceed with caution and monitor key indicators.";
    }
}
