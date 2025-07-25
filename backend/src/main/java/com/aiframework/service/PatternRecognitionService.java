package com.aiframework.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class PatternRecognitionService {

    public Map<String, Object> detectPatterns(List<Map<String, Object>> historicalData) {
        Map<String, Object> patterns = new HashMap<>();
        
        if (historicalData.size() < 20) {
            patterns.put("error", "Insufficient data for pattern recognition");
            return patterns;
        }

        patterns.put("support_resistance", findSupportResistance(historicalData));
        patterns.put("trend_lines", detectTrendLines(historicalData));
        patterns.put("chart_patterns", detectChartPatterns(historicalData));
        patterns.put("candlestick_patterns", detectCandlestickPatterns(historicalData));
        patterns.put("fibonacci_levels", calculateFibonacciLevels(historicalData));
        
        return patterns;
    }

    private Map<String, Object> findSupportResistance(List<Map<String, Object>> data) {
        Map<String, Object> levels = new HashMap<>();
        List<Double> highs = new ArrayList<>();
        List<Double> lows = new ArrayList<>();
        
        for (Map<String, Object> day : data) {
            highs.add((Double) day.get("high"));
            lows.add((Double) day.get("low"));
        }
        
        double resistance = highs.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double support = lows.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        
        levels.put("resistance", resistance);
        levels.put("support", support);
        levels.put("strength", calculateLevelStrength(data, resistance, support));
        
        return levels;
    }

    private Map<String, Object> detectTrendLines(List<Map<String, Object>> data) {
        Map<String, Object> trendLines = new HashMap<>();
        
        // Simple trend line detection
        double firstPrice = (Double) data.get(0).get("close");
        double lastPrice = (Double) data.get(data.size() - 1).get("close");
        double slope = (lastPrice - firstPrice) / data.size();
        
        trendLines.put("trend", slope > 0 ? "UPTREND" : slope < 0 ? "DOWNTREND" : "SIDEWAYS");
        trendLines.put("slope", slope);
        trendLines.put("start_price", firstPrice);
        trendLines.put("end_price", lastPrice);
        
        return trendLines;
    }

    private List<Map<String, Object>> detectChartPatterns(List<Map<String, Object>> data) {
        List<Map<String, Object>> patterns = new ArrayList<>();
        
        // Head and Shoulders pattern detection
        if (detectHeadAndShoulders(data)) {
            Map<String, Object> pattern = new HashMap<>();
            pattern.put("name", "Head and Shoulders");
            pattern.put("type", "REVERSAL");
            pattern.put("signal", "BEARISH");
            pattern.put("confidence", 0.75);
            patterns.add(pattern);
        }
        
        // Double Top/Bottom detection
        if (detectDoubleTop(data)) {
            Map<String, Object> pattern = new HashMap<>();
            pattern.put("name", "Double Top");
            pattern.put("type", "REVERSAL");
            pattern.put("signal", "BEARISH");
            pattern.put("confidence", 0.70);
            patterns.add(pattern);
        }
        
        // Triangle pattern detection
        if (detectTriangle(data)) {
            Map<String, Object> pattern = new HashMap<>();
            pattern.put("name", "Triangle");
            pattern.put("type", "CONTINUATION");
            pattern.put("signal", "NEUTRAL");
            pattern.put("confidence", 0.65);
            patterns.add(pattern);
        }
        
        return patterns;
    }

    private List<Map<String, Object>> detectCandlestickPatterns(List<Map<String, Object>> data) {
        List<Map<String, Object>> patterns = new ArrayList<>();
        
        for (int i = 1; i < data.size(); i++) {
            Map<String, Object> current = data.get(i);
            Map<String, Object> previous = data.get(i - 1);
            
            // Doji pattern
            if (isDoji(current)) {
                Map<String, Object> pattern = new HashMap<>();
                pattern.put("name", "Doji");
                pattern.put("signal", "INDECISION");
                pattern.put("index", i);
                patterns.add(pattern);
            }
            
            // Hammer pattern
            if (isHammer(current)) {
                Map<String, Object> pattern = new HashMap<>();
                pattern.put("name", "Hammer");
                pattern.put("signal", "BULLISH");
                pattern.put("index", i);
                patterns.add(pattern);
            }
            
            // Engulfing pattern
            if (isEngulfing(previous, current)) {
                Map<String, Object> pattern = new HashMap<>();
                pattern.put("name", "Engulfing");
                pattern.put("signal", "REVERSAL");
                pattern.put("index", i);
                patterns.add(pattern);
            }
        }
        
        return patterns;
    }

    private Map<String, Object> calculateFibonacciLevels(List<Map<String, Object>> data) {
        Map<String, Object> fibonacci = new HashMap<>();
        
        double high = data.stream().mapToDouble(d -> (Double) d.get("high")).max().orElse(0.0);
        double low = data.stream().mapToDouble(d -> (Double) d.get("low")).min().orElse(0.0);
        double range = high - low;
        
        Map<String, Double> levels = new HashMap<>();
        levels.put("0.0", high);
        levels.put("23.6", high - (range * 0.236));
        levels.put("38.2", high - (range * 0.382));
        levels.put("50.0", high - (range * 0.5));
        levels.put("61.8", high - (range * 0.618));
        levels.put("100.0", low);
        
        fibonacci.put("levels", levels);
        fibonacci.put("high", high);
        fibonacci.put("low", low);
        
        return fibonacci;
    }

    // Helper methods for pattern detection
    private double calculateLevelStrength(List<Map<String, Object>> data, double resistance, double support) {
        int touchCount = 0;
        double tolerance = (resistance - support) * 0.02; // 2% tolerance
        
        for (Map<String, Object> day : data) {
            double high = (Double) day.get("high");
            double low = (Double) day.get("low");
            
            if (Math.abs(high - resistance) <= tolerance || Math.abs(low - support) <= tolerance) {
                touchCount++;
            }
        }
        
        return Math.min(touchCount / 10.0, 1.0); // Normalize to 0-1
    }

    private boolean detectHeadAndShoulders(List<Map<String, Object>> data) {
        if (data.size() < 10) return false;
        
        // Simplified head and shoulders detection
        int midPoint = data.size() / 2;
        double headHigh = (Double) data.get(midPoint).get("high");
        double leftShoulderHigh = (Double) data.get(midPoint - 3).get("high");
        double rightShoulderHigh = (Double) data.get(midPoint + 3).get("high");
        
        return headHigh > leftShoulderHigh && headHigh > rightShoulderHigh &&
               Math.abs(leftShoulderHigh - rightShoulderHigh) / headHigh < 0.05;
    }

    private boolean detectDoubleTop(List<Map<String, Object>> data) {
        if (data.size() < 10) return false;
        
        double maxHigh = data.stream().mapToDouble(d -> (Double) d.get("high")).max().orElse(0.0);
        long peakCount = data.stream()
            .mapToDouble(d -> (Double) d.get("high"))
            .filter(h -> Math.abs(h - maxHigh) / maxHigh < 0.02)
            .count();
        
        return peakCount >= 2;
    }

    private boolean detectTriangle(List<Map<String, Object>> data) {
        if (data.size() < 15) return false;
        
        // Check if highs are decreasing and lows are increasing (converging)
        List<Double> highs = data.stream().map(d -> (Double) d.get("high")).toList();
        List<Double> lows = data.stream().map(d -> (Double) d.get("low")).toList();
        
        double highSlope = calculateSlope(highs);
        double lowSlope = calculateSlope(lows);
        
        return highSlope < 0 && lowSlope > 0; // Converging lines
    }

    private boolean isDoji(Map<String, Object> candle) {
        double open = (Double) candle.get("open");
        double close = (Double) candle.get("close");
        double high = (Double) candle.get("high");
        double low = (Double) candle.get("low");
        
        double bodySize = Math.abs(close - open);
        double totalRange = high - low;
        
        return bodySize / totalRange < 0.1; // Body is less than 10% of total range
    }

    private boolean isHammer(Map<String, Object> candle) {
        double open = (Double) candle.get("open");
        double close = (Double) candle.get("close");
        double high = (Double) candle.get("high");
        double low = (Double) candle.get("low");
        
        double bodySize = Math.abs(close - open);
        double lowerShadow = Math.min(open, close) - low;
        double upperShadow = high - Math.max(open, close);
        
        return lowerShadow > bodySize * 2 && upperShadow < bodySize * 0.5;
    }

    private boolean isEngulfing(Map<String, Object> previous, Map<String, Object> current) {
        double prevOpen = (Double) previous.get("open");
        double prevClose = (Double) previous.get("close");
        double currOpen = (Double) current.get("open");
        double currClose = (Double) current.get("close");
        
        boolean prevBearish = prevClose < prevOpen;
        boolean currBullish = currClose > currOpen;
        
        return prevBearish && currBullish && 
               currOpen < prevClose && currClose > prevOpen;
    }

    private double calculateSlope(List<Double> values) {
        if (values.size() < 2) return 0;
        
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = values.size();
        
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values.get(i);
            sumXY += i * values.get(i);
            sumX2 += i * i;
        }
        
        return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    }
}