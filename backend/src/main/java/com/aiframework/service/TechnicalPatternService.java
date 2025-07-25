package com.aiframework.service;

import com.aiframework.dto.TechnicalIndicators;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class TechnicalPatternService {

    public enum PatternType {
        BULLISH_ENGULFING, BEARISH_ENGULFING, HAMMER, DOJI, SHOOTING_STAR,
        HEAD_AND_SHOULDERS, DOUBLE_TOP, DOUBLE_BOTTOM, TRIANGLE, WEDGE,
        SUPPORT_RESISTANCE, TREND_LINE, BREAKOUT
    }

    public static class Pattern {
        private PatternType type;
        private String description;
        private double confidence;
        private String signal; // BULLISH, BEARISH, NEUTRAL
        private List<String> implications;

        public Pattern(PatternType type, String description, double confidence, String signal) {
            this.type = type;
            this.description = description;
            this.confidence = confidence;
            this.signal = signal;
            this.implications = new ArrayList<>();
        }

        // Getters and setters
        public PatternType getType() { return type; }
        public void setType(PatternType type) { this.type = type; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public String getSignal() { return signal; }
        public void setSignal(String signal) { this.signal = signal; }

        public List<String> getImplications() { return implications; }
        public void setImplications(List<String> implications) { this.implications = implications; }
    }

    public List<Pattern> detectPatterns(List<Map<String, Object>> historicalData, TechnicalIndicators indicators) {
        List<Pattern> patterns = new ArrayList<>();

        if (historicalData == null || historicalData.size() < 10) {
            return patterns;
        }

        // Candlestick patterns
        patterns.addAll(detectCandlestickPatterns(historicalData));

        // Chart patterns
        patterns.addAll(detectChartPatterns(historicalData));

        // Technical indicator patterns
        patterns.addAll(detectIndicatorPatterns(indicators));

        return patterns;
    }

    private List<Pattern> detectCandlestickPatterns(List<Map<String, Object>> data) {
        List<Pattern> patterns = new ArrayList<>();

        if (data.size() < 2) return patterns;

        // Get last two candles for pattern detection
        Map<String, Object> current = data.get(data.size() - 1);
        Map<String, Object> previous = data.get(data.size() - 2);

        double currentOpen = Double.parseDouble(current.get("open").toString());
        double currentClose = Double.parseDouble(current.get("close").toString());
        double currentHigh = Double.parseDouble(current.get("high").toString());
        double currentLow = Double.parseDouble(current.get("low").toString());

        double prevOpen = Double.parseDouble(previous.get("open").toString());
        double prevClose = Double.parseDouble(previous.get("close").toString());
        double prevHigh = Double.parseDouble(previous.get("high").toString());
        double prevLow = Double.parseDouble(previous.get("low").toString());

        // Bullish Engulfing Pattern
        if (prevClose < prevOpen && // Previous candle is bearish
            currentClose > currentOpen && // Current candle is bullish
            currentOpen < prevClose && // Current opens below previous close
            currentClose > prevOpen) { // Current closes above previous open

            Pattern pattern = new Pattern(PatternType.BULLISH_ENGULFING,
                "Bullish Engulfing pattern detected", 0.75, "BULLISH");
            pattern.getImplications().add("Potential reversal from bearish to bullish trend");
            pattern.getImplications().add("Consider long positions");
            patterns.add(pattern);
        }

        // Bearish Engulfing Pattern
        if (prevClose > prevOpen && // Previous candle is bullish
            currentClose < currentOpen && // Current candle is bearish
            currentOpen > prevClose && // Current opens above previous close
            currentClose < prevOpen) { // Current closes below previous open

            Pattern pattern = new Pattern(PatternType.BEARISH_ENGULFING,
                "Bearish Engulfing pattern detected", 0.75, "BEARISH");
            pattern.getImplications().add("Potential reversal from bullish to bearish trend");
            pattern.getImplications().add("Consider short positions or profit taking");
            patterns.add(pattern);
        }

        // Hammer Pattern
        double body = Math.abs(currentClose - currentOpen);
        double lowerShadow = Math.min(currentOpen, currentClose) - currentLow;
        double upperShadow = currentHigh - Math.max(currentOpen, currentClose);

        if (lowerShadow > body * 2 && upperShadow < body * 0.1) {
            Pattern pattern = new Pattern(PatternType.HAMMER,
                "Hammer pattern detected", 0.65, "BULLISH");
            pattern.getImplications().add("Potential bullish reversal signal");
            pattern.getImplications().add("Wait for confirmation in next session");
            patterns.add(pattern);
        }

        // Doji Pattern
        if (body < (currentHigh - currentLow) * 0.1) {
            Pattern pattern = new Pattern(PatternType.DOJI,
                "Doji pattern detected", 0.60, "NEUTRAL");
            pattern.getImplications().add("Market indecision");
            pattern.getImplications().add("Potential trend reversal or continuation");
            patterns.add(pattern);
        }

        return patterns;
    }

    private List<Pattern> detectChartPatterns(List<Map<String, Object>> data) {
        List<Pattern> patterns = new ArrayList<>();

        if (data.size() < 20) return patterns;

        List<Double> closes = data.stream()
                .map(d -> Double.parseDouble(d.get("close").toString()))
                .toList();

        List<Double> highs = data.stream()
                .map(d -> Double.parseDouble(d.get("high").toString()))
                .toList();

        List<Double> lows = data.stream()
                .map(d -> Double.parseDouble(d.get("low").toString()))
                .toList();

        // Support and Resistance Detection
        double currentPrice = closes.get(closes.size() - 1);
        double resistance = findResistanceLevel(highs);
        double support = findSupportLevel(lows);

        if (Math.abs(currentPrice - resistance) / currentPrice < 0.02) {
            Pattern pattern = new Pattern(PatternType.SUPPORT_RESISTANCE,
                "Approaching resistance level at " + String.format("%.2f", resistance), 0.70, "BEARISH");
            pattern.getImplications().add("Price may face selling pressure");
            pattern.getImplications().add("Watch for breakout or reversal");
            patterns.add(pattern);
        }

        if (Math.abs(currentPrice - support) / currentPrice < 0.02) {
            Pattern pattern = new Pattern(PatternType.SUPPORT_RESISTANCE,
                "Approaching support level at " + String.format("%.2f", support), 0.70, "BULLISH");
            pattern.getImplications().add("Price may find buying support");
            pattern.getImplications().add("Watch for bounce or breakdown");
            patterns.add(pattern);
        }

        // Triangle Pattern Detection
        if (detectTrianglePattern(highs, lows)) {
            Pattern pattern = new Pattern(PatternType.TRIANGLE,
                "Triangle consolidation pattern", 0.65, "NEUTRAL");
            pattern.getImplications().add("Price consolidating in triangle");
            pattern.getImplications().add("Breakout expected soon");
            patterns.add(pattern);
        }

        return patterns;
    }

    private List<Pattern> detectIndicatorPatterns(TechnicalIndicators indicators) {
        List<Pattern> patterns = new ArrayList<>();

        if (indicators == null) return patterns;

        // RSI Divergence
        if (indicators.getRsi() != null) {
            if (indicators.getRsi() > 80) {
                Pattern pattern = new Pattern(PatternType.BREAKOUT,
                    "Extremely overbought RSI condition", 0.80, "BEARISH");
                pattern.getImplications().add("High probability of price correction");
                pattern.getImplications().add("Consider profit taking or short positions");
                patterns.add(pattern);
            } else if (indicators.getRsi() < 20) {
                Pattern pattern = new Pattern(PatternType.BREAKOUT,
                    "Extremely oversold RSI condition", 0.80, "BULLISH");
                pattern.getImplications().add("High probability of price bounce");
                pattern.getImplications().add("Consider buying opportunities");
                patterns.add(pattern);
            }
        }

        // MACD Signal Line Crossover
        if (indicators.getMacd() != null && indicators.getMacdSignal() != null) {
            if (indicators.getMacd() > indicators.getMacdSignal() &&
                indicators.getMacdHistogram() > 0) {
                Pattern pattern = new Pattern(PatternType.BREAKOUT,
                    "MACD bullish crossover", 0.70, "BULLISH");
                pattern.getImplications().add("Momentum shifting to bullish");
                pattern.getImplications().add("Consider long positions");
                patterns.add(pattern);
            }
        }

        // Bollinger Band Squeeze
        if (indicators.getBollingerUpper() != null && indicators.getBollingerLower() != null) {
            double bandWidth = (indicators.getBollingerUpper() - indicators.getBollingerLower()) / indicators.getBollingerMiddle();
            if (bandWidth < 0.1) {
                Pattern pattern = new Pattern(PatternType.BREAKOUT,
                    "Bollinger Band squeeze detected", 0.75, "NEUTRAL");
                pattern.getImplications().add("Low volatility period");
                pattern.getImplications().add("Explosive move expected soon");
                patterns.add(pattern);
            }
        }

        return patterns;
    }

    private double findResistanceLevel(List<Double> highs) {
        // Simple resistance detection - find the highest high in recent period
        return highs.subList(Math.max(0, highs.size() - 20), highs.size())
                .stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }

    private double findSupportLevel(List<Double> lows) {
        // Simple support detection - find the lowest low in recent period
        return lows.subList(Math.max(0, lows.size() - 20), lows.size())
                .stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
    }

    private boolean detectTrianglePattern(List<Double> highs, List<Double> lows) {
        if (highs.size() < 10) return false;

        // Simple triangle detection - check if highs are declining and lows are rising
        List<Double> recentHighs = highs.subList(highs.size() - 10, highs.size());
        List<Double> recentLows = lows.subList(lows.size() - 10, lows.size());

        double highSlope = calculateSlope(recentHighs);
        double lowSlope = calculateSlope(recentLows);

        // Triangle if highs are declining and lows are rising (converging)
        return highSlope < -0.001 && lowSlope > 0.001;
    }

    private double calculateSlope(List<Double> values) {
        if (values.size() < 2) return 0;

        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        int n = values.size();

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values.get(i);
            sumXY += i * values.get(i);
            sumXX += i * i;
        }

        return (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
    }
}
