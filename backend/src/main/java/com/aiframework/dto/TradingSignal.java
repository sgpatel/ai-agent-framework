package com.aiframework.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class TradingSignal {
    public enum SignalType {
        BUY, SELL, HOLD, STRONG_BUY, STRONG_SELL
    }

    public enum SignalStrength {
        WEAK, MODERATE, STRONG, VERY_STRONG
    }

    private String symbol;
    private SignalType signal;
    private SignalStrength strength;
    private double confidence;
    private LocalDateTime timestamp;
    private String reason;
    private List<String> indicators;
    private Map<String, Object> metadata;

    // Price targets and risk management
    private Double targetPrice;
    private Double stopLoss;
    private Double riskRewardRatio;
    private String timeframe;

    public TradingSignal() {}

    public TradingSignal(String symbol, SignalType signal, SignalStrength strength, double confidence) {
        this.symbol = symbol;
        this.signal = signal;
        this.strength = strength;
        this.confidence = confidence;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public SignalType getSignal() { return signal; }
    public void setSignal(SignalType signal) { this.signal = signal; }

    public SignalStrength getStrength() { return strength; }
    public void setStrength(SignalStrength strength) { this.strength = strength; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public List<String> getIndicators() { return indicators; }
    public void setIndicators(List<String> indicators) { this.indicators = indicators; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public Double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(Double targetPrice) { this.targetPrice = targetPrice; }

    public Double getStopLoss() { return stopLoss; }
    public void setStopLoss(Double stopLoss) { this.stopLoss = stopLoss; }

    public Double getRiskRewardRatio() { return riskRewardRatio; }
    public void setRiskRewardRatio(Double riskRewardRatio) { this.riskRewardRatio = riskRewardRatio; }

    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
}
