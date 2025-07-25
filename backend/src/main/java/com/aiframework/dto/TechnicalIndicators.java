package com.aiframework.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class TechnicalIndicators {
    private String symbol;
    private LocalDateTime timestamp;

    // Moving Averages
    private Double sma20;
    private Double sma50;
    private Double sma200;
    private Double ema12;
    private Double ema26;

    // Momentum Indicators
    private Double rsi;
    private Double macd;
    private Double macdSignal;
    private Double macdHistogram;
    private Double stochasticK;
    private Double stochasticD;

    // Volatility Indicators
    private Double bollingerUpper;
    private Double bollingerMiddle;
    private Double bollingerLower;
    private Double atr;

    // Volume Indicators
    private Double volumeMA;
    private Double obv;

    // Support/Resistance
    private Double support;
    private Double resistance;

    // Additional indicators
    private Map<String, Double> additionalIndicators;

    public TechnicalIndicators() {}

    public TechnicalIndicators(String symbol) {
        this.symbol = symbol;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Double getSma20() { return sma20; }
    public void setSma20(Double sma20) { this.sma20 = sma20; }

    public Double getSma50() { return sma50; }
    public void setSma50(Double sma50) { this.sma50 = sma50; }

    public Double getSma200() { return sma200; }
    public void setSma200(Double sma200) { this.sma200 = sma200; }

    public Double getEma12() { return ema12; }
    public void setEma12(Double ema12) { this.ema12 = ema12; }

    public Double getEma26() { return ema26; }
    public void setEma26(Double ema26) { this.ema26 = ema26; }

    public Double getRsi() { return rsi; }
    public void setRsi(Double rsi) { this.rsi = rsi; }

    public Double getMacd() { return macd; }
    public void setMacd(Double macd) { this.macd = macd; }

    public Double getMacdSignal() { return macdSignal; }
    public void setMacdSignal(Double macdSignal) { this.macdSignal = macdSignal; }

    public Double getMacdHistogram() { return macdHistogram; }
    public void setMacdHistogram(Double macdHistogram) { this.macdHistogram = macdHistogram; }

    public Double getStochasticK() { return stochasticK; }
    public void setStochasticK(Double stochasticK) { this.stochasticK = stochasticK; }

    public Double getStochasticD() { return stochasticD; }
    public void setStochasticD(Double stochasticD) { this.stochasticD = stochasticD; }

    public Double getBollingerUpper() { return bollingerUpper; }
    public void setBollingerUpper(Double bollingerUpper) { this.bollingerUpper = bollingerUpper; }

    public Double getBollingerMiddle() { return bollingerMiddle; }
    public void setBollingerMiddle(Double bollingerMiddle) { this.bollingerMiddle = bollingerMiddle; }

    public Double getBollingerLower() { return bollingerLower; }
    public void setBollingerLower(Double bollingerLower) { this.bollingerLower = bollingerLower; }

    public Double getAtr() { return atr; }
    public void setAtr(Double atr) { this.atr = atr; }

    public Double getVolumeMA() { return volumeMA; }
    public void setVolumeMA(Double volumeMA) { this.volumeMA = volumeMA; }

    public Double getObv() { return obv; }
    public void setObv(Double obv) { this.obv = obv; }

    public Double getSupport() { return support; }
    public void setSupport(Double support) { this.support = support; }

    public Double getResistance() { return resistance; }
    public void setResistance(Double resistance) { this.resistance = resistance; }

    public Map<String, Double> getAdditionalIndicators() { return additionalIndicators; }
    public void setAdditionalIndicators(Map<String, Double> additionalIndicators) { this.additionalIndicators = additionalIndicators; }
}
