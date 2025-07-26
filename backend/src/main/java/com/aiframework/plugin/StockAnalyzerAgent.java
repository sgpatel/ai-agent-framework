package com.aiframework.plugin;

import com.aiframework.core.*;
import com.aiframework.context.ContextStore;
import com.aiframework.service.YahooFinanceService;
import com.aiframework.service.TechnicalIndicatorService;
import com.aiframework.service.PatternRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

@Component
public class StockAnalyzerAgent implements Agent {

    @Autowired(required = false)
    private ContextStore contextStore;

    private AgentConfig config;
    private AgentStatus status = AgentStatus.INITIALIZING;
    
    private YahooFinanceService yahooFinanceService;
    private TechnicalIndicatorService technicalIndicatorService;
    private PatternRecognitionService patternRecognitionService;

    @Override
    public String getName() {
        return "StockAnalyzer";
    }

    @Override
    public String getDescription() {
        return "Advanced stock analysis with technical indicators, sentiment analysis, and trading signals";
    }

    @Override
    public boolean canHandle(Task task) {
        if (task.getType() == null) return false;
        
        String taskType = task.getType().toLowerCase();
        String description = task.getDescription() != null ? task.getDescription().toLowerCase() : "";
        
        return "STOCK_ANALYSIS".equals(task.getType()) || 
               "FINANCIAL_ANALYSIS".equals(task.getType()) ||
               taskType.contains("stock") ||
               taskType.contains("analyse") ||
               taskType.contains("analyze") ||
               description.contains("stock") ||
               description.contains("apple") ||
               description.contains("tesla") ||
               description.contains("google") ||
               description.contains("microsoft") ||
               description.contains("amazon") ||
               description.contains("analysis");
    }

    @Override
    public void initialize(AgentConfig config) {
        this.config = config;
        // Create services directly since Spring DI doesn't work with ServiceLoader
        this.yahooFinanceService = new YahooFinanceService();
        this.technicalIndicatorService = new TechnicalIndicatorService();
        this.patternRecognitionService = new PatternRecognitionService();
        this.status = AgentStatus.READY;
    }

    @Override
    public void shutdown() {
        this.status = AgentStatus.SHUTDOWN;
    }

    @Override
    public AgentResult execute(Task task, AgentContext context) {
        try {
            status = AgentStatus.RUNNING;

            Map<String, Object> params = task.getParameters();
            String symbol = extractSymbolFromTask(task, params);
            String period = (String) params.getOrDefault("period", "1y");
            String indicators = (String) params.getOrDefault("indicators", "sma,rsi");
            String analysisType = (String) params.getOrDefault("analysis_type", "all");

            // Store context for plugin collaboration
            if (contextStore != null) {
                contextStore.storeContext(getName(), "currentSymbol", symbol);
                contextStore.storeContext(getName(), "dataType", "stock-analysis");
                contextStore.storeContext(getName(), "analysisType", analysisType);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("symbol", symbol);
            result.put("period", period);
            result.put("timestamp", LocalDate.now().toString());

            // Perform core analysis
            if ("technical".equals(analysisType) || "all".equals(analysisType)) {
                result.put("technical_analysis", performTechnicalAnalysis(symbol, indicators));
            }

            if ("fundamental".equals(analysisType) || "all".equals(analysisType)) {
                result.put("fundamental_analysis", performFundamentalAnalysis(symbol));
            }

            // Use Sentiment Analysis Plugin
            Map<String, Object> sentimentData = useSentimentAnalysisPlugin(symbol);
            if (sentimentData != null) {
                result.put("sentiment_analysis", sentimentData);
            }

            // Trading Signals and Risk Metrics
            result.put("trading_signals", generateTradingSignals(symbol));
            result.put("risk_metrics", calculateRiskMetrics(symbol));

            // Share data with other plugins
            shareDataWithPlugins(symbol, result);

            // Trigger chart visualization
            triggerChartVisualizationPlugin(symbol, result);

            status = AgentStatus.READY;
            return AgentResult.success(task.getId(), getName(), result);

        } catch (Exception e) {
            status = AgentStatus.ERROR;
            return AgentResult.failure(task.getId(), getName(), "Stock analysis failed: " + e.getMessage());
        }
    }

    /**
     * Use Sentiment Analysis Plugin for market sentiment
     */
    private Map<String, Object> useSentimentAnalysisPlugin(String symbol) {
        if (contextStore == null) return null;

        try {
            // Check if sentiment data is already available in shared context
            var existingSentiment = contextStore.getSharedData("marketSentiment");
            if (existingSentiment.isPresent()) {
                var sentimentEntry = existingSentiment.get();
                Map<String, Object> sentimentData = (Map<String, Object>) sentimentEntry.getData();
                if (symbol.equals(sentimentData.get("symbol"))) {
                    return sentimentData;
                }
            }

            // Trigger sentiment analysis plugin
            Task sentimentTask = new Task();
            sentimentTask.setId("sentiment-" + symbol + "-" + System.currentTimeMillis());
            sentimentTask.setType("SENTIMENT_ANALYSIS");
            sentimentTask.setDescription("Analyze market sentiment for " + symbol);
            sentimentTask.setParameters(Map.of("symbol", symbol, "timeframe", "24h"));

            // Note: In a real implementation, you'd use the orchestrator to execute this
            // For now, we'll check if sentiment data becomes available in context

            return null; // Sentiment plugin will share data via context

        } catch (Exception e) {
            System.err.println("Failed to use sentiment analysis plugin: " + e.getMessage());
            return null;
        }
    }

    /**
     * Share analysis data with other plugins
     */
    private void shareDataWithPlugins(String symbol, Map<String, Object> analysisResult) {
        if (contextStore == null) return;

        try {
            // Share stock data for other plugins to use
            contextStore.storeSharedData(
                "stockData",
                analysisResult,
                getName(),
                Map.of(
                    "dataType", "time-series",
                    "symbol", symbol,
                    "canVisualize", true,
                    "hasIndicators", true,
                    "analysisComplete", true
                )
            );

            // Share financial metrics for risk assessment
            if (analysisResult.containsKey("risk_metrics")) {
                contextStore.storeSharedData(
                    "financialMetrics",
                    analysisResult.get("risk_metrics"),
                    getName(),
                    Map.of(
                        "dataType", "financial-data",
                        "symbol", symbol,
                        "suitable_for", Arrays.asList("risk-assessment", "portfolio-optimization")
                    )
                );
            }

        } catch (Exception e) {
            System.err.println("Failed to share data with plugins: " + e.getMessage());
        }
    }

    /**
     * Trigger Chart Visualization Plugin
     */
    private void triggerChartVisualizationPlugin(String symbol, Map<String, Object> stockData) {
        if (contextStore == null) return;

        try {
            // Set context to trigger chart creation
            contextStore.storeContext("chart-visualizer", "chartRequest", Map.of(
                "symbol", symbol,
                "chartType", "candlestick",
                "dataSource", "stockData",
                "requestedBy", getName(),
                "timestamp", System.currentTimeMillis()
            ));

            // Create chart task (this would be handled by orchestrator in real implementation)
            Map<String, Object> chartRequest = Map.of(
                "type", "CHART_CREATION",
                "chartType", "candlestick",
                "symbol", symbol,
                "dataSource", "stockData"
            );

            contextStore.storeSharedData(
                "chartRequest",
                chartRequest,
                getName(),
                Map.of("targetAgent", "chart-visualizer", "priority", "high")
            );

        } catch (Exception e) {
            System.err.println("Failed to trigger chart visualization: " + e.getMessage());
        }
    }

    private Map<String, Object> performTechnicalAnalysis(String symbol, String indicators) {
        try {
            // Get real historical data
            List<Map<String, Object>> historicalData = yahooFinanceService.getHistoricalData(symbol, "3mo");
            
            // Calculate real technical indicators
            Map<String, Object> technical = technicalIndicatorService.calculateIndicators(historicalData, indicators);
            
            // Add trend analysis
            double currentPrice = yahooFinanceService.getRealTimePrice(symbol).get("price") != null ? 
                (Double) yahooFinanceService.getRealTimePrice(symbol).get("price") : 0.0;
            double sma20 = technical.get("sma_20") != null ? (Double) technical.get("sma_20") : currentPrice;
            
            technical.put("trend", currentPrice > sma20 ? "BULLISH" : "BEARISH");
            technical.put("current_price", currentPrice);
            
            // Add pattern recognition
            Map<String, Object> patterns = patternRecognitionService.detectPatterns(historicalData);
            technical.put("patterns", patterns);
            
            return technical;
        } catch (Exception e) {
            // Fallback to mock data if API fails
            Map<String, Object> technical = new HashMap<>();
            technical.put("error", "Failed to fetch real data: " + e.getMessage());
            technical.put("sma_20", 150.25);
            technical.put("rsi", 65.4);
            return technical;
        }
    }

    private Map<String, Object> performFundamentalAnalysis(String symbol) {
        try {
            Map<String, Object> priceData = yahooFinanceService.getRealTimePrice(symbol);
            Map<String, Object> fundamental = new HashMap<>();
            
            // Use real market cap from Yahoo Finance
            fundamental.put("market_cap", priceData.get("market_cap"));
            fundamental.put("current_price", priceData.get("price"));
            fundamental.put("volume", priceData.get("volume"));
            fundamental.put("currency", priceData.get("currency"));
            
            // Mock ratios (would need additional API for full fundamental data)
            fundamental.put("pe_ratio", 28.5);
            fundamental.put("pb_ratio", 6.2);
            fundamental.put("note", "Full fundamental data requires premium API access");
            
            return fundamental;
        } catch (Exception e) {
            Map<String, Object> fundamental = new HashMap<>();
            fundamental.put("error", "Failed to fetch fundamental data: " + e.getMessage());
            return fundamental;
        }
    }

    private Map<String, Object> performSentimentAnalysis(String symbol) {
        Map<String, Object> sentiment = new HashMap<>();
        
        // Mock sentiment data - replace with real news/social media analysis
        sentiment.put("overall_sentiment", "POSITIVE");
        sentiment.put("sentiment_score", 0.72);
        sentiment.put("news_sentiment", 0.68);
        sentiment.put("social_sentiment", 0.75);
        sentiment.put("analyst_rating", "BUY");
        sentiment.put("price_target", 165.0);
        
        return sentiment;
    }

    private Map<String, Object> generateTradingSignals(String symbol) {
        try {
            Map<String, Object> priceData = yahooFinanceService.getRealTimePrice(symbol);
            double currentPrice = (Double) priceData.get("price");
            double changePercent = (Double) priceData.get("change_percent");
            
            Map<String, Object> signals = new HashMap<>();
            
            // Simple signal logic based on price movement and RSI
            String signal = "HOLD";
            double confidence = 0.5;
            
            if (changePercent > 2.0) {
                signal = "BUY";
                confidence = 0.75;
            } else if (changePercent < -2.0) {
                signal = "SELL";
                confidence = 0.70;
            }
            
            signals.put("signal", signal);
            signals.put("confidence", confidence);
            signals.put("entry_price", currentPrice);
            signals.put("stop_loss", currentPrice * 0.95);
            signals.put("take_profit", currentPrice * 1.08);
            signals.put("position_size", "5%");
            signals.put("reasoning", "Based on " + String.format("%.2f", changePercent) + "% price change");
            
            return signals;
        } catch (Exception e) {
            Map<String, Object> signals = new HashMap<>();
            signals.put("error", "Failed to generate signals: " + e.getMessage());
            signals.put("signal", "HOLD");
            return signals;
        }
    }

    private Map<String, Object> calculateRiskMetrics(String symbol) {
        Map<String, Object> risk = new HashMap<>();
        
        risk.put("volatility", 0.24);
        risk.put("beta", 1.2);
        risk.put("var_95", -0.035);
        risk.put("sharpe_ratio", 1.45);
        risk.put("max_drawdown", -0.18);
        
        return risk;
    }

    private String extractSymbolFromTask(Task task, Map<String, Object> params) {
        // First check parameters
        if (params.containsKey("symbol")) {
            return (String) params.get("symbol");
        }
        
        // Extract from description
        if (task.getDescription() == null) return "AAPL";
        String description = task.getDescription().toLowerCase();
        if (description.contains("apple")) return "AAPL";
        if (description.contains("tesla")) return "TSLA";
        if (description.contains("google") || description.contains("alphabet")) return "GOOGL";
        if (description.contains("microsoft")) return "MSFT";
        if (description.contains("amazon")) return "AMZN";
        if (description.contains("meta") || description.contains("facebook")) return "META";
        if (description.contains("netflix")) return "NFLX";
        if (description.contains("nvidia")) return "NVDA";
        
        // Look for stock symbols in uppercase
        String[] words = task.getDescription().split("\\s+");
        for (String word : words) {
            if (word.matches("[A-Z]{1,5}")) {
                return word;
            }
        }
        
        return "AAPL"; // Default fallback
    }

    @Override
    public AgentStatus getStatus() {
        return status;
    }

    @Override
    public AgentConfig getConfig() {
        return config;
    }
}
