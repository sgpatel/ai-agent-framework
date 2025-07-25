package com.aiframework.service;

import com.aiframework.dto.DecisionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class LLMDecisionEngine {

    private static final Logger logger = LoggerFactory.getLogger(LLMDecisionEngine.class);

    @Autowired
    private LocalLLMService localLLMService;

    public CompletableFuture<DecisionResult> makeInvestmentDecision(String symbol, Map<String, Object> marketData) {
        String decisionPrompt = buildInvestmentDecisionPrompt(symbol, marketData);

        return localLLMService.generateResponse(decisionPrompt, 512, 0.3f)
            .thenApply(response -> parseDecisionResponse(response, "INVESTMENT"));
    }

    public CompletableFuture<DecisionResult> assessRiskLevel(String symbol, Map<String, Object> indicators) {
        String riskPrompt = buildRiskAssessmentPrompt(symbol, indicators);

        return localLLMService.generateResponse(riskPrompt, 384, 0.2f)
            .thenApply(response -> parseDecisionResponse(response, "RISK_ASSESSMENT"));
    }

    public CompletableFuture<DecisionResult> generateTradingStrategy(String symbol, Map<String, Object> context) {
        String strategyPrompt = buildTradingStrategyPrompt(symbol, context);

        return localLLMService.generateResponse(strategyPrompt, 768, 0.4f)
            .thenApply(response -> parseDecisionResponse(response, "TRADING_STRATEGY"));
    }

    public CompletableFuture<DecisionResult> analyzeMarketSentiment(Map<String, Object> marketConditions) {
        String sentimentPrompt = buildMarketSentimentPrompt(marketConditions);

        return localLLMService.generateResponse(sentimentPrompt, 512, 0.3f)
            .thenApply(response -> parseDecisionResponse(response, "MARKET_SENTIMENT"));
    }

    private String buildInvestmentDecisionPrompt(String symbol, Map<String, Object> marketData) {
        return String.format("""
            As an AI investment advisor, analyze the following data for %s and provide a clear investment decision:
            
            Market Data: %s
            
            Consider:
            1. Technical indicators and their signals
            2. Price trends and volume analysis
            3. Risk-reward ratio
            4. Market conditions
            5. Entry/exit points
            
            Provide your decision in this JSON format:
            {
                "decision": "BUY|SELL|HOLD",
                "confidence": 0.85,
                "reasoning": "Clear explanation of the decision",
                "actionItems": ["specific action 1", "specific action 2"],
                "riskLevel": "LOW|MEDIUM|HIGH",
                "timeHorizon": "SHORT|MEDIUM|LONG",
                "stopLoss": 123.45,
                "targetPrice": 234.56
            }
            """, symbol, formatMarketData(marketData));
    }

    private String buildRiskAssessmentPrompt(String symbol, Map<String, Object> indicators) {
        return String.format("""
            Perform a comprehensive risk assessment for %s based on these indicators:
            
            Technical Indicators: %s
            
            Evaluate:
            1. Volatility risk
            2. Market correlation risk
            3. Liquidity risk
            4. Technical breakdown risk
            5. Sector/industry specific risks
            
            Respond in JSON format:
            {
                "overallRisk": "LOW|MEDIUM|HIGH|EXTREME",
                "riskScore": 7.5,
                "riskFactors": ["factor1", "factor2"],
                "mitigationStrategies": ["strategy1", "strategy2"],
                "warningSignals": ["signal1", "signal2"],
                "recommendation": "specific risk management advice"
            }
            """, symbol, formatIndicators(indicators));
    }

    private String buildTradingStrategyPrompt(String symbol, Map<String, Object> context) {
        return String.format("""
            Create a comprehensive trading strategy for %s based on current conditions:
            
            Context: %s
            
            Generate strategy covering:
            1. Entry criteria and timing
            2. Position sizing
            3. Risk management rules
            4. Exit strategy
            5. Monitoring requirements
            
            Format as JSON:
            {
                "strategyName": "Descriptive name",
                "entryConditions": ["condition1", "condition2"],
                "positionSize": "2%% of portfolio",
                "stopLoss": 123.45,
                "takeProfit": [234.56, 345.67],
                "timeframe": "1D|4H|1H",
                "riskReward": 2.5,
                "monitoringPoints": ["point1", "point2"],
                "contingencyPlan": "backup strategy"
            }
            """, symbol, formatContext(context));
    }

    private String buildMarketSentimentPrompt(Map<String, Object> marketConditions) {
        return String.format("""
            Analyze overall market sentiment and conditions:
            
            Market Conditions: %s
            
            Assess:
            1. Overall market sentiment (bullish/bearish)
            2. Sector rotation trends
            3. Economic indicators impact
            4. Volatility environment
            5. Risk-on vs risk-off sentiment
            
            JSON response:
            {
                "sentiment": "BULLISH|BEARISH|NEUTRAL",
                "sentimentScore": 6.5,
                "keyDrivers": ["driver1", "driver2"],
                "sectorsInFocus": ["sector1", "sector2"],
                "marketRegime": "TRENDING|RANGING|VOLATILE",
                "outlook": "SHORT|MEDIUM|LONG term outlook",
                "actionableInsights": ["insight1", "insight2"]
            }
            """, formatMarketConditions(marketConditions));
    }

    private DecisionResult parseDecisionResponse(String response, String decisionType) {
        try {
            // Simple JSON-like parsing (you could use Jackson for more robust parsing)
            DecisionResult result = new DecisionResult();
            result.setDecisionType(decisionType);
            result.setRawResponse(response);
            result.setTimestamp(new Date());
            result.setConfidence(extractConfidence(response));
            result.setRecommendation(extractRecommendation(response));
            result.setActionItems(extractActionItems(response));
            result.setRiskLevel(extractRiskLevel(response));

            return result;
        } catch (Exception e) {
            logger.error("Error parsing decision response: {}", e.getMessage());
            return createErrorResult(decisionType, "Error parsing LLM response");
        }
    }

    private double extractConfidence(String response) {
        try {
            // Extract confidence from JSON-like response
            if (response.contains("\"confidence\":")) {
                String confidenceStr = response.substring(
                    response.indexOf("\"confidence\":") + 13,
                    response.indexOf(",", response.indexOf("\"confidence\":"))
                ).trim();
                return Double.parseDouble(confidenceStr);
            }
        } catch (Exception e) {
            logger.debug("Could not extract confidence: {}", e.getMessage());
        }
        return 0.5; // Default confidence
    }

    private String extractRecommendation(String response) {
        if (response.contains("\"decision\":")) {
            try {
                String decision = response.substring(
                    response.indexOf("\"decision\":") + 12,
                    response.indexOf(",", response.indexOf("\"decision\":"))
                ).replaceAll("[\"\\s]", "");
                return decision;
            } catch (Exception e) {
                logger.debug("Could not extract decision: {}", e.getMessage());
            }
        }
        return "HOLD"; // Default recommendation
    }

    private List<String> extractActionItems(String response) {
        // Extract action items from response
        List<String> actions = new ArrayList<>();
        if (response.contains("\"actionItems\":")) {
            // Simple extraction - in production, use proper JSON parsing
            actions.add("Monitor key indicators");
            actions.add("Review position sizing");
        }
        return actions;
    }

    private String extractRiskLevel(String response) {
        if (response.contains("\"riskLevel\":") || response.contains("\"overallRisk\":")) {
            // Extract risk level from response
            String[] riskLevels = {"LOW", "MEDIUM", "HIGH", "EXTREME"};
            for (String level : riskLevels) {
                if (response.toUpperCase().contains(level)) {
                    return level;
                }
            }
        }
        return "MEDIUM"; // Default risk level
    }

    private DecisionResult createErrorResult(String decisionType, String errorMessage) {
        DecisionResult result = new DecisionResult();
        result.setDecisionType(decisionType);
        result.setRawResponse(errorMessage);
        result.setTimestamp(new Date());
        result.setConfidence(0.0);
        result.setRecommendation("ERROR");
        result.setRiskLevel("HIGH");
        return result;
    }

    private String formatMarketData(Map<String, Object> data) {
        return data.toString(); // Simplify for now
    }

    private String formatIndicators(Map<String, Object> indicators) {
        return indicators.toString();
    }

    private String formatContext(Map<String, Object> context) {
        return context.toString();
    }

    private String formatMarketConditions(Map<String, Object> conditions) {
        return conditions.toString();
    }
}
