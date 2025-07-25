package com.aiframework.service;

import com.aiframework.dto.StockQuote;
import com.aiframework.dto.TechnicalIndicators;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PricePredictionService {

    public static class PricePrediction {
        private String symbol;
        private LocalDateTime timestamp;
        private double currentPrice;
        private Map<String, Double> predictions; // timeframe -> predicted price
        private Map<String, Double> confidence; // timeframe -> confidence level
        private String trend; // BULLISH, BEARISH, SIDEWAYS
        private double trendStrength;
        private List<String> predictionFactors;
        private Map<String, Double> scenarioAnalysis; // scenario -> probability

        public PricePrediction(String symbol, double currentPrice) {
            this.symbol = symbol;
            this.currentPrice = currentPrice;
            this.timestamp = LocalDateTime.now();
            this.predictions = new HashMap<>();
            this.confidence = new HashMap<>();
            this.predictionFactors = new ArrayList<>();
            this.scenarioAnalysis = new HashMap<>();
        }

        // Getters and setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public double getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

        public Map<String, Double> getPredictions() { return predictions; }
        public void setPredictions(Map<String, Double> predictions) { this.predictions = predictions; }

        public Map<String, Double> getConfidence() { return confidence; }
        public void setConfidence(Map<String, Double> confidence) { this.confidence = confidence; }

        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }

        public double getTrendStrength() { return trendStrength; }
        public void setTrendStrength(double trendStrength) { this.trendStrength = trendStrength; }

        public List<String> getPredictionFactors() { return predictionFactors; }
        public void setPredictionFactors(List<String> predictionFactors) { this.predictionFactors = predictionFactors; }

        public Map<String, Double> getScenarioAnalysis() { return scenarioAnalysis; }
        public void setScenarioAnalysis(Map<String, Double> scenarioAnalysis) { this.scenarioAnalysis = scenarioAnalysis; }
    }

    public static class AlertRule {
        private String id;
        private String symbol;
        private String type; // PRICE_ABOVE, PRICE_BELOW, RSI_OVERBOUGHT, RSI_OVERSOLD, VOLUME_SPIKE, etc.
        private double threshold;
        private boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime lastTriggered;
        private String message;

        public AlertRule(String symbol, String type, double threshold, String message) {
            this.id = UUID.randomUUID().toString();
            this.symbol = symbol;
            this.type = type;
            this.threshold = threshold;
            this.message = message;
            this.isActive = true;
            this.createdAt = LocalDateTime.now();
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public double getThreshold() { return threshold; }
        public void setThreshold(double threshold) { this.threshold = threshold; }

        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getLastTriggered() { return lastTriggered; }
        public void setLastTriggered(LocalDateTime lastTriggered) { this.lastTriggered = lastTriggered; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    private final Map<String, List<AlertRule>> alertRules = new HashMap<>();

    public PricePrediction predictPrice(String symbol, StockQuote quote, TechnicalIndicators indicators,
                                      List<Map<String, Object>> historicalData) {
        PricePrediction prediction = new PricePrediction(symbol, quote.getPrice());

        // Calculate trend and strength
        calculateTrend(prediction, historicalData, indicators);

        // Generate price predictions for different timeframes
        generatePricePredictions(prediction, quote, indicators, historicalData);

        // Calculate confidence levels
        calculateConfidenceLevels(prediction, indicators);

        // Add prediction factors
        addPredictionFactors(prediction, indicators);

        // Scenario analysis
        performScenarioAnalysis(prediction, indicators);

        return prediction;
    }

    private void calculateTrend(PricePrediction prediction, List<Map<String, Object>> historicalData,
                               TechnicalIndicators indicators) {
        if (historicalData == null || historicalData.size() < 10) {
            prediction.setTrend("SIDEWAYS");
            prediction.setTrendStrength(0.3);
            return;
        }

        // Calculate price trend using linear regression
        List<Double> prices = historicalData.stream()
                .map(d -> Double.parseDouble(d.get("close").toString()))
                .toList();

        double slope = calculatePriceSlope(prices);

        if (slope > 0.02) {
            prediction.setTrend("BULLISH");
            prediction.setTrendStrength(Math.min(slope * 10, 1.0));
        } else if (slope < -0.02) {
            prediction.setTrend("BEARISH");
            prediction.setTrendStrength(Math.min(Math.abs(slope) * 10, 1.0));
        } else {
            prediction.setTrend("SIDEWAYS");
            prediction.setTrendStrength(0.3);
        }

        // Adjust trend based on technical indicators
        if (indicators != null) {
            adjustTrendWithIndicators(prediction, indicators);
        }
    }

    private void generatePricePredictions(PricePrediction prediction, StockQuote quote,
                                        TechnicalIndicators indicators, List<Map<String, Object>> historicalData) {
        double currentPrice = prediction.getCurrentPrice();

        // Simple prediction model based on trend and technical indicators
        double trendMultiplier = getTrendMultiplier(prediction.getTrend(), prediction.getTrendStrength());
        double volatilityAdjustment = getVolatilityAdjustment(historicalData);

        // 1-day prediction
        double dayPrediction = currentPrice * (1 + trendMultiplier * 0.01 + (Math.random() - 0.5) * volatilityAdjustment);
        prediction.getPredictions().put("1D", dayPrediction);

        // 1-week prediction
        double weekPrediction = currentPrice * (1 + trendMultiplier * 0.05 + (Math.random() - 0.5) * volatilityAdjustment * 2);
        prediction.getPredictions().put("1W", weekPrediction);

        // 1-month prediction
        double monthPrediction = currentPrice * (1 + trendMultiplier * 0.15 + (Math.random() - 0.5) * volatilityAdjustment * 3);
        prediction.getPredictions().put("1M", monthPrediction);

        // 3-month prediction
        double quarterPrediction = currentPrice * (1 + trendMultiplier * 0.35 + (Math.random() - 0.5) * volatilityAdjustment * 5);
        prediction.getPredictions().put("3M", quarterPrediction);

        // Adjust predictions based on technical indicators
        if (indicators != null) {
            adjustPredictionsWithIndicators(prediction, indicators);
        }
    }

    private void calculateConfidenceLevels(PricePrediction prediction, TechnicalIndicators indicators) {
        double baseConfidence = 0.6;

        // Confidence decreases with time horizon
        prediction.getConfidence().put("1D", Math.min(baseConfidence + 0.2, 1.0));
        prediction.getConfidence().put("1W", baseConfidence);
        prediction.getConfidence().put("1M", Math.max(baseConfidence - 0.1, 0.3));
        prediction.getConfidence().put("3M", Math.max(baseConfidence - 0.2, 0.2));

        // Adjust confidence based on trend strength
        double trendAdjustment = prediction.getTrendStrength() * 0.2;
        for (String timeframe : prediction.getConfidence().keySet()) {
            double adjustedConfidence = prediction.getConfidence().get(timeframe) + trendAdjustment;
            prediction.getConfidence().put(timeframe, Math.min(adjustedConfidence, 1.0));
        }

        // Adjust based on technical indicator alignment
        if (indicators != null) {
            double indicatorAlignment = calculateIndicatorAlignment(indicators);
            for (String timeframe : prediction.getConfidence().keySet()) {
                double adjustedConfidence = prediction.getConfidence().get(timeframe) + indicatorAlignment * 0.15;
                prediction.getConfidence().put(timeframe, Math.min(adjustedConfidence, 1.0));
            }
        }
    }

    private void addPredictionFactors(PricePrediction prediction, TechnicalIndicators indicators) {
        prediction.getPredictionFactors().add("Trend analysis: " + prediction.getTrend() +
                                            " with strength " + String.format("%.2f", prediction.getTrendStrength()));

        if (indicators != null) {
            if (indicators.getRsi() != null) {
                if (indicators.getRsi() > 70) {
                    prediction.getPredictionFactors().add("RSI indicates overbought conditions");
                } else if (indicators.getRsi() < 30) {
                    prediction.getPredictionFactors().add("RSI indicates oversold conditions");
                }
            }

            if (indicators.getMacd() != null && indicators.getMacdSignal() != null) {
                if (indicators.getMacd() > indicators.getMacdSignal()) {
                    prediction.getPredictionFactors().add("MACD showing bullish momentum");
                } else {
                    prediction.getPredictionFactors().add("MACD showing bearish momentum");
                }
            }

            if (indicators.getSma20() != null && indicators.getSma50() != null) {
                if (indicators.getSma20() > indicators.getSma50()) {
                    prediction.getPredictionFactors().add("Short-term moving averages above long-term");
                } else {
                    prediction.getPredictionFactors().add("Short-term moving averages below long-term");
                }
            }
        }
    }

    private void performScenarioAnalysis(PricePrediction prediction, TechnicalIndicators indicators) {
        // Bull case scenario
        prediction.getScenarioAnalysis().put("Bull Case (+20%)", 0.25);
        prediction.getScenarioAnalysis().put("Moderate Bull (+10%)", 0.35);
        prediction.getScenarioAnalysis().put("Base Case (+2%)", 0.40);
        prediction.getScenarioAnalysis().put("Bear Case (-10%)", 0.20);
        prediction.getScenarioAnalysis().put("Extreme Bear (-20%)", 0.10);

        // Adjust probabilities based on current conditions
        if (indicators != null && indicators.getRsi() != null) {
            if (indicators.getRsi() > 80) {
                // Overbought - increase bear case probabilities
                prediction.getScenarioAnalysis().put("Bear Case (-10%)", 0.35);
                prediction.getScenarioAnalysis().put("Extreme Bear (-20%)", 0.20);
                prediction.getScenarioAnalysis().put("Bull Case (+20%)", 0.15);
            } else if (indicators.getRsi() < 20) {
                // Oversold - increase bull case probabilities
                prediction.getScenarioAnalysis().put("Bull Case (+20%)", 0.35);
                prediction.getScenarioAnalysis().put("Moderate Bull (+10%)", 0.45);
                prediction.getScenarioAnalysis().put("Bear Case (-10%)", 0.10);
            }
        }
    }

    // Alert system methods
    public String createAlert(String symbol, String type, double threshold, String message) {
        AlertRule rule = new AlertRule(symbol, type, threshold, message);
        alertRules.computeIfAbsent(symbol, k -> new ArrayList<>()).add(rule);
        return rule.getId();
    }

    public List<String> checkAlerts(String symbol, StockQuote quote, TechnicalIndicators indicators) {
        List<String> triggeredAlerts = new ArrayList<>();
        List<AlertRule> rules = alertRules.get(symbol);

        if (rules == null || rules.isEmpty()) {
            return triggeredAlerts;
        }

        for (AlertRule rule : rules) {
            if (!rule.isActive()) continue;

            boolean triggered = false;

            switch (rule.getType()) {
                case "PRICE_ABOVE":
                    triggered = quote.getPrice() > rule.getThreshold();
                    break;
                case "PRICE_BELOW":
                    triggered = quote.getPrice() < rule.getThreshold();
                    break;
                case "RSI_OVERBOUGHT":
                    triggered = indicators.getRsi() != null && indicators.getRsi() > rule.getThreshold();
                    break;
                case "RSI_OVERSOLD":
                    triggered = indicators.getRsi() != null && indicators.getRsi() < rule.getThreshold();
                    break;
                case "VOLUME_SPIKE":
                    triggered = quote.getVolume() > rule.getThreshold();
                    break;
                case "VOLATILITY_HIGH":
                    triggered = indicators.getAtr() != null && indicators.getAtr() > rule.getThreshold();
                    break;
            }

            if (triggered) {
                rule.setLastTriggered(LocalDateTime.now());
                triggeredAlerts.add(rule.getMessage());
            }
        }

        return triggeredAlerts;
    }

    public boolean removeAlert(String alertId) {
        for (List<AlertRule> rules : alertRules.values()) {
            rules.removeIf(rule -> rule.getId().equals(alertId));
        }
        return true;
    }

    public List<AlertRule> getActiveAlerts(String symbol) {
        return alertRules.getOrDefault(symbol, new ArrayList<>())
                .stream()
                .filter(AlertRule::isActive)
                .toList();
    }

    // Helper methods
    private double calculatePriceSlope(List<Double> prices) {
        if (prices.size() < 2) return 0;

        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        int n = Math.min(prices.size(), 20); // Use last 20 data points

        for (int i = 0; i < n; i++) {
            int index = prices.size() - n + i;
            sumX += i;
            sumY += prices.get(index);
            sumXY += i * prices.get(index);
            sumXX += i * i;
        }

        return (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
    }

    private void adjustTrendWithIndicators(PricePrediction prediction, TechnicalIndicators indicators) {
        double adjustment = 0;

        if (indicators.getRsi() != null) {
            if (indicators.getRsi() > 70) adjustment -= 0.1; // Overbought
            if (indicators.getRsi() < 30) adjustment += 0.1; // Oversold
        }

        if (indicators.getMacd() != null && indicators.getMacdSignal() != null) {
            if (indicators.getMacd() > indicators.getMacdSignal()) adjustment += 0.05;
            else adjustment -= 0.05;
        }

        prediction.setTrendStrength(Math.max(0, Math.min(1, prediction.getTrendStrength() + adjustment)));
    }

    private double getTrendMultiplier(String trend, double strength) {
        switch (trend) {
            case "BULLISH": return strength;
            case "BEARISH": return -strength;
            default: return 0;
        }
    }

    private double getVolatilityAdjustment(List<Map<String, Object>> historicalData) {
        if (historicalData == null || historicalData.size() < 10) return 0.02;

        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < historicalData.size(); i++) {
            double current = Double.parseDouble(historicalData.get(i).get("close").toString());
            double previous = Double.parseDouble(historicalData.get(i-1).get("close").toString());
            returns.add((current - previous) / previous);
        }

        return calculateStandardDeviation(returns);
    }

    private void adjustPredictionsWithIndicators(PricePrediction prediction, TechnicalIndicators indicators) {
        double adjustment = 1.0;

        if (indicators.getRsi() != null) {
            if (indicators.getRsi() > 80) adjustment *= 0.95; // Reduce predictions if extremely overbought
            if (indicators.getRsi() < 20) adjustment *= 1.05; // Increase predictions if extremely oversold
        }

        for (String timeframe : prediction.getPredictions().keySet()) {
            double adjustedPrice = prediction.getPredictions().get(timeframe) * adjustment;
            prediction.getPredictions().put(timeframe, adjustedPrice);
        }
    }

    private double calculateIndicatorAlignment(TechnicalIndicators indicators) {
        int bullishSignals = 0;
        int bearishSignals = 0;
        int totalSignals = 0;

        if (indicators.getRsi() != null) {
            totalSignals++;
            if (indicators.getRsi() < 30) bullishSignals++;
            if (indicators.getRsi() > 70) bearishSignals++;
        }

        if (indicators.getMacd() != null && indicators.getMacdSignal() != null) {
            totalSignals++;
            if (indicators.getMacd() > indicators.getMacdSignal()) bullishSignals++;
            else bearishSignals++;
        }

        if (totalSignals == 0) return 0;

        double alignment = Math.abs(bullishSignals - bearishSignals) / (double) totalSignals;
        return alignment;
    }

    private double calculateStandardDeviation(List<Double> values) {
        if (values.isEmpty()) return 0.0;

        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average().orElse(0.0);

        return Math.sqrt(variance);
    }
}
