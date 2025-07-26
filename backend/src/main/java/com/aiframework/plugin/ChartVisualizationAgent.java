package com.aiframework.plugin;

import com.aiframework.core.*;
import com.aiframework.context.ContextStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.time.LocalDateTime;

/**
 * Chart Visualization Plugin
 * Creates interactive charts from shared data provided by other agents
 */
@Component
public class ChartVisualizationAgent implements Agent {

    @Autowired(required = false)
    private ContextStore contextStore;

    private AgentConfig config;
    private AgentStatus status = AgentStatus.INITIALIZING;

    @Override
    public String getName() {
        return "ChartVisualizer";
    }

    @Override
    public String getDescription() {
        return "Creates interactive charts and visualizations from stock data, sentiment analysis, and other data sources";
    }

    @Override
    public boolean canHandle(Task task) {
        if (task.getType() == null) return false;

        String taskType = task.getType().toLowerCase();
        String description = task.getDescription() != null ? task.getDescription().toLowerCase() : "";

        return "CHART_CREATION".equals(task.getType()) ||
               "VISUALIZATION".equals(task.getType()) ||
               taskType.contains("chart") ||
               taskType.contains("graph") ||
               taskType.contains("visualiz") ||
               description.contains("chart") ||
               description.contains("plot");
    }

    @Override
    public AgentResult execute(Task task, AgentContext context) {
        try {
            status = AgentStatus.RUNNING;

            Map<String, Object> parameters = task.getParameters();
            String chartType = (String) parameters.getOrDefault("chartType", "line");
            String dataSource = (String) parameters.get("dataSource");

            // Get shared data from other agents
            Map<String, Object> chartData = new HashMap<>();
            if (contextStore != null) {
                // Check for stock data from Stock Analyzer
                var stockData = contextStore.getSharedData("stockData");
                if (stockData.isPresent()) {
                    chartData.put("stockData", stockData.get());
                }

                // Check for sentiment data from Sentiment Analyzer
                var sentimentData = contextStore.getSharedData("marketSentiment");
                if (sentimentData.isPresent()) {
                    chartData.put("sentimentData", sentimentData.get());
                }

                // Store our context
                contextStore.storeContext(getName(), "chartType", chartType);
                contextStore.storeContext(getName(), "dataType", "chart-visualization");
            }

            // Create the chart configuration
            Map<String, Object> chartConfig = createChartConfiguration(chartType, chartData, parameters);

            // Share chart configuration with frontend
            if (contextStore != null) {
                contextStore.storeSharedData(
                    "chartConfiguration",
                    chartConfig,
                    getName(),
                    Map.of(
                        "dataType", "chart-config",
                        "chartType", chartType,
                        "interactive", true
                    )
                );
            }

            status = AgentStatus.READY;

            return AgentResult.success(
                task.getId(),
                getName(),
                chartConfig
            );

        } catch (Exception e) {
            status = AgentStatus.ERROR;
            return AgentResult.failure(
                task.getId(),
                getName(),
                "Chart creation failed: " + e.getMessage()
            );
        }
    }

    private Map<String, Object> createChartConfiguration(String chartType, Map<String, Object> data, Map<String, Object> parameters) {
        Map<String, Object> config = new HashMap<>();

        config.put("type", chartType);
        config.put("createdAt", LocalDateTime.now().toString());
        config.put("interactive", true);

        // Configure based on chart type and available data
        switch (chartType.toLowerCase()) {
            case "candlestick":
                config.putAll(createCandlestickConfig(data));
                break;
            case "line":
                config.putAll(createLineChartConfig(data));
                break;
            case "sentiment":
                config.putAll(createSentimentChartConfig(data));
                break;
            case "volume":
                config.putAll(createVolumeChartConfig(data));
                break;
            default:
                config.putAll(createDefaultChartConfig(data));
        }

        // Add interaction options
        config.put("options", createInteractionOptions(chartType));

        return config;
    }

    private Map<String, Object> createCandlestickConfig(Map<String, Object> data) {
        Map<String, Object> config = new HashMap<>();
        config.put("chartType", "candlestick");
        config.put("title", "Stock Price Analysis");
        config.put("yAxis", Map.of("title", "Price ($)", "type", "linear"));
        config.put("xAxis", Map.of("title", "Date", "type", "datetime"));

        // Configure data series
        List<Map<String, Object>> series = new ArrayList<>();
        if (data.containsKey("stockData")) {
            series.add(Map.of(
                "name", "OHLC",
                "type", "candlestick",
                "data", "stockData.historical" // Reference to actual data
            ));
        }
        config.put("series", series);

        return config;
    }

    private Map<String, Object> createSentimentChartConfig(Map<String, Object> data) {
        Map<String, Object> config = new HashMap<>();
        config.put("chartType", "sentiment");
        config.put("title", "Market Sentiment Analysis");

        // Create sentiment gauge
        config.put("gauge", Map.of(
            "min", -1,
            "max", 1,
            "value", data.containsKey("sentimentData") ? "sentimentData.overallSentiment" : 0,
            "zones", Arrays.asList(
                Map.of("from", -1, "to", -0.3, "color", "#ff4444"),
                Map.of("from", -0.3, "to", 0.3, "color", "#ffaa00"),
                Map.of("from", 0.3, "to", 1, "color", "#44ff44")
            )
        ));

        return config;
    }

    private Map<String, Object> createLineChartConfig(Map<String, Object> data) {
        Map<String, Object> config = new HashMap<>();
        config.put("chartType", "line");
        config.put("title", "Stock Price Trend");

        return config;
    }

    private Map<String, Object> createVolumeChartConfig(Map<String, Object> data) {
        Map<String, Object> config = new HashMap<>();
        config.put("chartType", "volume");
        config.put("title", "Trading Volume");

        return config;
    }

    private Map<String, Object> createDefaultChartConfig(Map<String, Object> data) {
        return Map.of(
            "chartType", "default",
            "title", "Data Visualization"
        );
    }

    private Map<String, Object> createInteractionOptions(String chartType) {
        Map<String, Object> options = new HashMap<>();
        options.put("zoom", true);
        options.put("pan", true);
        options.put("crosshair", true);
        options.put("legend", true);

        // Chart-specific interactions
        switch (chartType.toLowerCase()) {
            case "candlestick":
                options.put("technicalIndicators", Arrays.asList("SMA", "RSI", "MACD"));
                options.put("drawingTools", Arrays.asList("trendline", "support", "resistance"));
                break;
            case "sentiment":
                options.put("timeframe", Arrays.asList("1h", "1d", "1w", "1m"));
                break;
        }

        return options;
    }

    @Override
    public void initialize(AgentConfig config) {
        this.config = config;
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

    @Override
    public AgentConfig getConfig() {
        return config;
    }
}
