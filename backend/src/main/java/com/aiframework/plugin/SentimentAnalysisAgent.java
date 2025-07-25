package com.aiframework.plugin;

import com.aiframework.core.*;
import com.aiframework.context.ContextStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.time.LocalDateTime;

/**
 * Sentiment Analysis Plugin
 * Analyzes market sentiment from news, social media, and other sources
 */
@Component
public class SentimentAnalysisAgent implements Agent {

    @Autowired(required = false)
    private ContextStore contextStore;

    @Autowired(required = false)
    private RestTemplate restTemplate;

    private AgentConfig config;
    private AgentStatus status = AgentStatus.INITIALIZING;

    // Plugin configuration
    private String newsApiKey;
    private String sentimentApiEndpoint;
    private boolean enableSocialMediaAnalysis = true;

    @Override
    public String getName() {
        return "SentimentAnalyzer";
    }

    @Override
    public String getDescription() {
        return "Analyzes market sentiment from news articles, social media, and financial reports";
    }

    @Override
    public boolean canHandle(Task task) {
        if (task.getType() == null) return false;

        String taskType = task.getType().toLowerCase();
        String description = task.getDescription() != null ? task.getDescription().toLowerCase() : "";

        return "SENTIMENT_ANALYSIS".equals(task.getType()) ||
               "MARKET_SENTIMENT".equals(task.getType()) ||
               taskType.contains("sentiment") ||
               taskType.contains("mood") ||
               taskType.contains("opinion") ||
               description.contains("sentiment") ||
               description.contains("news analysis");
    }

    @Override
    public AgentResult execute(Task task, AgentContext context) {
        try {
            status = AgentStatus.RUNNING;

            // Get parameters
            Map<String, Object> parameters = task.getParameters();
            String symbol = (String) parameters.get("symbol");
            String timeframe = (String) parameters.getOrDefault("timeframe", "24h");

            // Store context for collaboration
            if (contextStore != null) {
                contextStore.storeContext(getName(), "currentAnalysis", symbol);
                contextStore.storeContext(getName(), "dataType", "sentiment-analysis");
                contextStore.storeContext(getName(), "timeframe", timeframe);
            }

            // Perform sentiment analysis
            Map<String, Object> sentimentData = analyzeSentiment(symbol, timeframe);

            // Share results with other agents (e.g., Stock Analyzer)
            if (contextStore != null) {
                contextStore.storeSharedData(
                    "marketSentiment",
                    sentimentData,
                    getName(),
                    Map.of(
                        "dataType", "sentiment-data",
                        "symbol", symbol,
                        "timeframe", timeframe,
                        "canVisualize", true
                    )
                );
            }

            status = AgentStatus.READY;

            return AgentResult.success(
                task.getId(),
                getName(),
                sentimentData
            );

        } catch (Exception e) {
            status = AgentStatus.ERROR;
            return AgentResult.failure(
                task.getId(),
                getName(),
                "Sentiment analysis failed: " + e.getMessage()
            );
        }
    }

    private Map<String, Object> analyzeSentiment(String symbol, String timeframe) {
        Map<String, Object> sentimentData = new HashMap<>();

        // Analyze news sentiment
        Map<String, Object> newsAnalysis = analyzeNewsSentiment(symbol);

        // Analyze social media sentiment (if enabled)
        Map<String, Object> socialAnalysis = new HashMap<>();
        if (enableSocialMediaAnalysis) {
            socialAnalysis = analyzeSocialMediaSentiment(symbol);
        }

        // Calculate overall sentiment score
        double overallSentiment = calculateOverallSentiment(newsAnalysis, socialAnalysis);
        String sentimentLabel = getSentimentLabel(overallSentiment);

        sentimentData.put("symbol", symbol);
        sentimentData.put("timeframe", timeframe);
        sentimentData.put("overallSentiment", overallSentiment);
        sentimentData.put("sentimentLabel", sentimentLabel);
        sentimentData.put("newsAnalysis", newsAnalysis);
        sentimentData.put("socialAnalysis", socialAnalysis);
        sentimentData.put("analyzedAt", LocalDateTime.now().toString());
        sentimentData.put("confidence", calculateConfidence(newsAnalysis, socialAnalysis));

        return sentimentData;
    }

    private Map<String, Object> analyzeNewsSentiment(String symbol) {
        // Mock implementation - replace with real news API integration
        Map<String, Object> newsData = new HashMap<>();
        newsData.put("articlesAnalyzed", 25);
        newsData.put("positiveArticles", 15);
        newsData.put("negativeArticles", 7);
        newsData.put("neutralArticles", 3);
        newsData.put("averageSentiment", 0.32); // -1 to 1 scale
        newsData.put("sources", Arrays.asList("Reuters", "Bloomberg", "Yahoo Finance", "MarketWatch"));
        return newsData;
    }

    private Map<String, Object> analyzeSocialMediaSentiment(String symbol) {
        // Mock implementation - replace with social media API integration
        Map<String, Object> socialData = new HashMap<>();
        socialData.put("tweetsAnalyzed", 1250);
        socialData.put("positiveMentions", 780);
        socialData.put("negativeMentions", 320);
        socialData.put("neutralMentions", 150);
        socialData.put("averageSentiment", 0.18);
        socialData.put("trendingHashtags", Arrays.asList("#" + symbol, "#investing", "#stocks"));
        return socialData;
    }

    private double calculateOverallSentiment(Map<String, Object> news, Map<String, Object> social) {
        double newsSentiment = (Double) news.getOrDefault("averageSentiment", 0.0);
        double socialSentiment = (Double) social.getOrDefault("averageSentiment", 0.0);

        // Weight news sentiment more heavily (70% news, 30% social)
        return (newsSentiment * 0.7) + (socialSentiment * 0.3);
    }

    private String getSentimentLabel(double sentiment) {
        if (sentiment > 0.3) return "Very Positive";
        if (sentiment > 0.1) return "Positive";
        if (sentiment > -0.1) return "Neutral";
        if (sentiment > -0.3) return "Negative";
        return "Very Negative";
    }

    private double calculateConfidence(Map<String, Object> news, Map<String, Object> social) {
        int newsArticles = (Integer) news.getOrDefault("articlesAnalyzed", 0);
        int socialMentions = (Integer) social.getOrDefault("tweetsAnalyzed", 0);

        // Higher confidence with more data points
        double dataScore = Math.min(1.0, (newsArticles + socialMentions / 10.0) / 100.0);
        return Math.round(dataScore * 100.0) / 100.0;
    }

    @Override
    public void initialize(AgentConfig config) {
        this.config = config;

        // Initialize configuration from plugin settings
        if (config != null && config.getProperties() != null) {
            this.newsApiKey = (String) config.getProperties().get("news.api.key");
            this.sentimentApiEndpoint = (String) config.getProperties().get("sentiment.api.endpoint");
            this.enableSocialMediaAnalysis = Boolean.parseBoolean(
                (String) config.getProperties().getOrDefault("social.analysis.enabled", "true")
            );
        }

        this.status = AgentStatus.READY;
    }

    @Override
    public void shutdown() {
        this.status = AgentStatus.SHUTDOWN;
    }

    @Override
    public AgentStatus getStatus() {
        return status;
    }

   // @Override
    public AgentConfig getConfig() {
        return config;
    }
}
