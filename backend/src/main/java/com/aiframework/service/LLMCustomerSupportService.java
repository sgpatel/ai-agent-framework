package com.aiframework.service;

import com.aiframework.dto.SupportResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class LLMCustomerSupportService {

    private static final Logger logger = LoggerFactory.getLogger(LLMCustomerSupportService.class);

    @Autowired
    private LocalLLMService localLLMService;

    @Autowired
    private LLMDecisionEngine decisionEngine;

    public CompletableFuture<SupportResponse> handleStockAlert(String symbol, String alertType, Map<String, Object> marketData) {
        String alertPrompt = buildStockAlertPrompt(symbol, alertType, marketData);

        return localLLMService.generateResponse(alertPrompt, 768, 0.4f)
            .thenApply(response -> createSupportResponse(response, "STOCK_ALERT", symbol));
    }

    public CompletableFuture<SupportResponse> explainTechnicalIndicator(String indicator, String symbol, Map<String, Object> context) {
        String explanationPrompt = buildIndicatorExplanationPrompt(indicator, symbol, context);

        return localLLMService.generateResponse(explanationPrompt, 512, 0.3f)
            .thenApply(response -> createSupportResponse(response, "INDICATOR_EXPLANATION", symbol));
    }

    public CompletableFuture<SupportResponse> provideRiskWarning(String symbol, String riskType, Map<String, Object> riskData) {
        String warningPrompt = buildRiskWarningPrompt(symbol, riskType, riskData);

        return localLLMService.generateResponse(warningPrompt, 640, 0.2f)
            .thenApply(response -> createSupportResponse(response, "RISK_WARNING", symbol));
    }

    public CompletableFuture<SupportResponse> suggestActionPlan(String symbol, String situation, Map<String, Object> portfolio) {
        String actionPrompt = buildActionPlanPrompt(symbol, situation, portfolio);

        return localLLMService.generateResponse(actionPrompt, 896, 0.4f)
            .thenApply(response -> createSupportResponse(response, "ACTION_PLAN", symbol));
    }

    public CompletableFuture<SupportResponse> handleGeneralInquiry(String question, Map<String, Object> userContext) {
        String inquiryPrompt = buildGeneralInquiryPrompt(question, userContext);

        return localLLMService.generateResponse(inquiryPrompt, 512, 0.5f)
            .thenApply(response -> createSupportResponse(response, "GENERAL_INQUIRY", "N/A"));
    }

    private String buildStockAlertPrompt(String symbol, String alertType, Map<String, Object> marketData) {
        return String.format("""
            A stock alert has been triggered for %s. Provide customer support guidance:
            
            Alert Type: %s
            Market Data: %s
            
            As a helpful financial assistant, provide:
            1. Clear explanation of what this alert means
            2. Immediate actions the customer should consider
            3. Risk assessment and warnings if applicable
            4. Next steps and monitoring recommendations
            5. Educational content about this type of alert
            
            Format your response as helpful, actionable customer support that includes:
            - Clear, non-technical explanation
            - Specific action recommendations
            - Risk warnings where appropriate
            - Educational insights
            - Reassurance and confidence building
            
            Maintain a supportive, professional tone.
            """, symbol, alertType, formatMarketData(marketData));
    }

    private String buildIndicatorExplanationPrompt(String indicator, String symbol, Map<String, Object> context) {
        return String.format("""
            A customer needs help understanding the %s indicator for %s:
            
            Current Context: %s
            
            Provide a comprehensive but accessible explanation covering:
            1. What this indicator measures in simple terms
            2. How to interpret the current reading
            3. What it suggests about %s right now
            4. How this fits into their overall analysis
            5. Common mistakes to avoid with this indicator
            6. Actionable next steps
            
            Make it educational and practical, avoiding jargon while being thorough.
            Include specific examples related to the current situation.
            """, indicator, symbol, formatContext(context), symbol);
    }

    private String buildRiskWarningPrompt(String symbol, String riskType, Map<String, Object> riskData) {
        return String.format("""
            Important: Risk warning needs to be communicated for %s
            
            Risk Type: %s
            Risk Data: %s
            
            Create a clear, urgent but not panic-inducing warning message that includes:
            1. Clear statement of the identified risk
            2. Potential impact on their investment
            3. Immediate protective actions they can take
            4. How to monitor the situation going forward
            5. When to seek additional help
            6. Resources for learning more about this risk type
            
            Balance urgency with reassurance. Be specific about actions.
            Help them understand this is manageable with proper response.
            """, symbol, riskType, formatRiskData(riskData));
    }

    private String buildActionPlanPrompt(String symbol, String situation, Map<String, Object> portfolio) {
        return String.format("""
            Customer needs a clear action plan for %s in this situation:
            
            Current Situation: %s
            Portfolio Context: %s
            
            Develop a step-by-step action plan that includes:
            1. Immediate actions (next 24-48 hours)
            2. Short-term monitoring plan (next 1-2 weeks)
            3. Medium-term strategy adjustments
            4. Risk management steps
            5. Decision checkpoints and criteria
            6. Resources and tools to use
            7. When to reassess the plan
            
            Make it actionable, specific, and confidence-building.
            Include timeline and priority levels for each action.
            """, symbol, situation, formatPortfolio(portfolio));
    }

    private String buildGeneralInquiryPrompt(String question, Map<String, Object> userContext) {
        return String.format("""
            Customer Question: %s
            
            User Context: %s
            
            Provide helpful, accurate financial guidance that:
            1. Directly answers their question
            2. Provides relevant context and background
            3. Offers practical next steps
            4. Includes relevant warnings or considerations
            5. Suggests additional resources if helpful
            6. Maintains professional, supportive tone
            
            If the question is outside financial advice scope, politely redirect
            while still being maximally helpful within appropriate boundaries.
            """, question, formatUserContext(userContext));
    }

    private SupportResponse createSupportResponse(String llmResponse, String responseType, String symbol) {
        SupportResponse response = new SupportResponse();
        response.setResponseType(responseType);
        response.setSymbol(symbol);
        response.setContent(llmResponse);
        response.setTimestamp(new Date());
        response.setUrgencyLevel(determineUrgencyLevel(responseType, llmResponse));
        response.setFollowUpRequired(determineFollowUpNeeded(llmResponse));
        response.setActionItems(extractActionItems(llmResponse));
        response.setEducationalContent(extractEducationalContent(llmResponse));

        return response;
    }

    private String determineUrgencyLevel(String responseType, String content) {
        if (responseType.equals("RISK_WARNING") || content.toLowerCase().contains("urgent") ||
            content.toLowerCase().contains("immediate")) {
            return "HIGH";
        } else if (responseType.equals("STOCK_ALERT") || responseType.equals("ACTION_PLAN")) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private boolean determineFollowUpNeeded(String content) {
        return content.toLowerCase().contains("monitor") ||
               content.toLowerCase().contains("check back") ||
               content.toLowerCase().contains("reassess");
    }

    private List<String> extractActionItems(String content) {
        List<String> actions = new ArrayList<>();
        // Simple extraction - in production, use more sophisticated NLP
        if (content.toLowerCase().contains("immediate")) {
            actions.add("Take immediate action as outlined");
        }
        if (content.toLowerCase().contains("monitor")) {
            actions.add("Set up monitoring alerts");
        }
        if (content.toLowerCase().contains("review")) {
            actions.add("Review portfolio allocation");
        }
        return actions;
    }

    private String extractEducationalContent(String content) {
        // Extract educational sections from the response
        if (content.contains("educational") || content.contains("learn")) {
            return "Educational content available in full response";
        }
        return null;
    }

    private String formatMarketData(Map<String, Object> data) {
        return data.toString();
    }

    private String formatContext(Map<String, Object> context) {
        return context.toString();
    }

    private String formatRiskData(Map<String, Object> riskData) {
        return riskData.toString();
    }

    private String formatPortfolio(Map<String, Object> portfolio) {
        return portfolio.toString();
    }

    private String formatUserContext(Map<String, Object> userContext) {
        return userContext.toString();
    }
}

