package com.aiframework.service;

import com.aiframework.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class IntelligentSystemIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(IntelligentSystemIntegrationService.class);

    @Autowired
    private LLMDecisionEngine decisionEngine;

    @Autowired
    private LLMCustomerSupportService customerSupport;

    @Autowired
    private IntelligentAgentCommunicationHub communicationHub;

    @Autowired
    private LocalLLMService localLLMService;

    @Autowired
    private StockDataService stockDataService;

    @Autowired
    private TechnicalIndicatorService technicalIndicatorService;

    /**
     * Main orchestration method that demonstrates the complete intelligent workflow
     */
    public CompletableFuture<IntelligentAnalysisResult> performIntelligentAnalysis(String symbol, String userIntent) {
        logger.info("Starting intelligent analysis for {} with intent: {}", symbol, userIntent);

        return collectMarketData(symbol)
            .thenCompose(marketData -> routeToAppropriateAgents(symbol, userIntent, marketData))
            .thenCompose(this::synthesizeIntelligentResponse)
            .thenCompose(result -> provideCustomerSupport(result))
            .exceptionally(this::handleAnalysisError);
    }

    /**
     * Demonstrates how agents can dynamically communicate based on market conditions
     */
    public CompletableFuture<Void> handleMarketEventIntelligently(String symbol, String eventType, Map<String, Object> eventData) {
        // Step 1: Assess if this requires immediate customer notification
        return assessEventUrgency(symbol, eventType, eventData)
            .thenCompose(urgency -> {
                if ("HIGH".equals(urgency)) {
                    // Immediately trigger customer support for high urgency events
                    return customerSupport.handleStockAlert(symbol, eventType, eventData)
                        .thenCompose(supportResponse -> notifyCustomer(supportResponse));
                } else {
                    // Route through normal agent communication for analysis
                    AgentMessage message = new AgentMessage("market-monitor",
                        "Analyze market event: " + eventType + " for " + symbol, eventData);
                    return communicationHub.routeMessage(message).thenApply(result -> null);
                }
            });
    }

    /**
     * Shows how any agent can talk to any other agent without hardcoded relationships
     */
    public CompletableFuture<String> facilitateDynamicAgentCollaboration(String initiatingTask, Map<String, Object> context) {
        // The LLM determines which agents should collaborate based on the task context
        return communicationHub.findBestAgentsForTask(initiatingTask, context)
            .thenCompose(selectedAgents -> {
                logger.info("LLM selected agents for task '{}': {}", initiatingTask, selectedAgents);

                // Create collaboration between selected agents
                return communicationHub.facilitateAgentCollaboration("task-initiator", initiatingTask, selectedAgents)
                    .thenApply(collaboration ->
                        "Dynamic collaboration established between: " + String.join(", ", selectedAgents));
            });
    }

    private CompletableFuture<Map<String, Object>> collectMarketData(String symbol) {
        // Integrate with your existing stock data services
        Map<String, Object> marketData = new HashMap<>();
        marketData.put("symbol", symbol);
        marketData.put("timestamp", new Date());

        // This would call your actual stock data services
        try {
            // Example integration with existing services
            marketData.put("currentPrice", 150.25);
            marketData.put("volume", 1000000);
            marketData.put("technicalIndicators", getTechnicalIndicators(symbol));

            return CompletableFuture.completedFuture(marketData);
        } catch (Exception e) {
            logger.error("Error collecting market data for {}", symbol, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private Map<String, Object> getTechnicalIndicators(String symbol) {
        // This would integrate with your TechnicalIndicatorService
        Map<String, Object> indicators = new HashMap<>();
        indicators.put("rsi", 65.5);
        indicators.put("macd", 0.75);
        indicators.put("movingAverage20", 148.5);
        indicators.put("movingAverage50", 145.2);
        indicators.put("bollingerBands", Map.of("upper", 155.0, "lower", 145.0));
        return indicators;
    }

    private CompletableFuture<AgentCommunicationResult> routeToAppropriateAgents(String symbol, String userIntent, Map<String, Object> marketData) {
        // Create a task that includes user intent and market context
        String task = String.format("Analyze %s based on user intent: %s", symbol, userIntent);

        Map<String, Object> context = new HashMap<>(marketData);
        context.put("userIntent", userIntent);
        context.put("analysisType", determineAnalysisType(userIntent));

        AgentMessage message = new AgentMessage("user-interface", task, context);
        return communicationHub.routeMessage(message);
    }

    private String determineAnalysisType(String userIntent) {
        userIntent = userIntent.toLowerCase();
        if (userIntent.contains("risk")) return "RISK_FOCUSED";
        if (userIntent.contains("buy") || userIntent.contains("sell")) return "TRADING_FOCUSED";
        if (userIntent.contains("long term")) return "INVESTMENT_FOCUSED";
        if (userIntent.contains("pattern")) return "TECHNICAL_FOCUSED";
        return "COMPREHENSIVE";
    }

    private CompletableFuture<IntelligentAnalysisResult> synthesizeIntelligentResponse(AgentCommunicationResult agentResult) {
        // Use LLM to create a coherent response from multiple agent outputs
        IntelligentAnalysisResult result = new IntelligentAnalysisResult();
        result.setAgentResults(agentResult.getAgentResults());
        result.setSynthesizedAnalysis(agentResult.getSynthesizedResult());
        result.setTimestamp(new Date());

        // Determine if any warnings or immediate actions are needed
        result.setRequiresAttention(assessIfAttentionRequired(agentResult));
        result.setRecommendedActions(extractRecommendedActions(agentResult));

        return CompletableFuture.completedFuture(result);
    }

    private CompletableFuture<IntelligentAnalysisResult> provideCustomerSupport(IntelligentAnalysisResult result) {
        if (result.isRequiresAttention()) {
            // Automatically generate customer support response
            String supportContext = "Analysis indicates attention required: " + result.getSynthesizedAnalysis();
            return customerSupport.handleGeneralInquiry(supportContext,
                Map.of("analysisResult", result))
                .thenApply(supportResponse -> {
                    result.setCustomerSupportResponse(supportResponse);
                    return result;
                });
        }
        return CompletableFuture.completedFuture(result);
    }

    private CompletableFuture<String> assessEventUrgency(String symbol, String eventType, Map<String, Object> eventData) {
        // Use LLM to assess urgency of market events
        String urgencyPrompt = String.format("""
            Assess the urgency level of this market event:
            
            Symbol: %s
            Event Type: %s
            Event Data: %s
            
            Respond with just one word: LOW, MEDIUM, or HIGH
            
            Consider:
            - Price movements > 5%% = HIGH
            - Volume spikes > 200%% = MEDIUM
            - Technical breakouts = MEDIUM
            - News-related events = varies based on impact
            """, symbol, eventType, eventData.toString());

        return localLLMService.generateResponse(urgencyPrompt, 50, 0.1f)
            .thenApply(response -> {
                String urgency = String.valueOf(response).trim().toUpperCase();
                return Arrays.asList("LOW", "MEDIUM", "HIGH").contains(urgency) ? urgency : "MEDIUM";
            });
    }

    private CompletableFuture<Void> notifyCustomer(SupportResponse supportResponse) {
        // This would integrate with your notification system
        logger.info("Customer notification triggered: {} - {}",
            supportResponse.getUrgencyLevel(),
            supportResponse.getContent().substring(0, Math.min(100, supportResponse.getContent().length())));
        return CompletableFuture.completedFuture(null);
    }

    private boolean assessIfAttentionRequired(AgentCommunicationResult result) {
        String analysis = result.getSynthesizedResult();
        if (analysis == null) return false;

        String lowerAnalysis = analysis.toLowerCase();
        return lowerAnalysis.contains("warning") ||
               lowerAnalysis.contains("urgent") ||
               lowerAnalysis.contains("immediate") ||
               lowerAnalysis.contains("risk");
    }

    private List<String> extractRecommendedActions(AgentCommunicationResult result) {
        // Extract actionable items from the analysis
        List<String> actions = new ArrayList<>();
        if (result.getSynthesizedResult() != null) {
            // Simple extraction - in production, use more sophisticated NLP
            actions.add("Review analysis recommendations");
            actions.add("Monitor key indicators");
            actions.add("Consider risk management measures");
        }
        return actions;
    }

    private IntelligentAnalysisResult handleAnalysisError(Throwable throwable) {
        logger.error("Error in intelligent analysis", throwable);

        IntelligentAnalysisResult errorResult = new IntelligentAnalysisResult();
        errorResult.setError(true);
        errorResult.setErrorMessage(throwable.getMessage());
        errorResult.setTimestamp(new Date());

        return errorResult;
    }
}
